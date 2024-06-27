package com.outsystems.plugins.inappbrowser.osinappbrowserlib.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABEvents
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.R
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import kotlinx.coroutines.launch

class OSIABWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var closeButton: Button
    private lateinit var backNavigationButton: ImageButton
    private lateinit var forwardNavigationButton: ImageButton
    private lateinit var urlText: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var options: OSIABWebViewOptions
    private lateinit var appName: String
    // for the browserPageLoaded event, which we only want to trigger on the first URL loaded in the WebView
    private var isFirstLoad = true

    // permissions
    private var currentPermissionRequest: PermissionRequest? = null

    //geolocation permissions
    private var geolocationCallback: GeolocationPermissions.Callback? = null
    private var geolocationOrigin: String? = null

    companion object {
        const val WEB_VIEW_URL_EXTRA = "WEB_VIEW_URL_EXTRA"
        const val WEB_VIEW_OPTIONS_EXTRA = "WEB_VIEW_OPTIONS_EXTRA"
        const val REQUEST_STANDARD_PERMISSION = 622
        const val REQUEST_LOCATION_PERMISSION = 623
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appName = applicationInfo.loadLabel(packageManager).toString()

        // get parameters from intent extras
        val urlToOpen = intent.extras?.getString(WEB_VIEW_URL_EXTRA)
        options = intent.extras?.getSerializable(WEB_VIEW_OPTIONS_EXTRA) as OSIABWebViewOptions

        setContentView(R.layout.activity_web_view)

        //get elements in screen
        webView = findViewById(R.id.webview)
        closeButton = findViewById(R.id.close_button)
        backNavigationButton = findViewById(R.id.back_button)
        forwardNavigationButton = findViewById(R.id.forward_button)
        urlText = findViewById(R.id.url_text)
        toolbar = findViewById(R.id.toolbar)

        // setup elements in screen
        closeButton.text = options.closeButtonText.ifBlank { "Close" }
        urlText.text = urlToOpen
        toolbar.isVisible = options.showToolbar

        // clear cache if necessary
        possiblyClearCacheOrSessionCookies()
        // enable third party cookies
        enableThirdPartyCookies()

        setupWebView()
        if (urlToOpen != null) {
            webView.loadUrl(urlToOpen)
        }

        closeButton.setOnClickListener {
            sendWebViewEvent(OSIABEvents.BrowserFinished)
            webView.destroy()
            finish()
        }

        backNavigationButton.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        forwardNavigationButton.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

    }

    override fun onPause() {
        super.onPause()
        if (options.pauseMedia) {
            webView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (options.pauseMedia) {
            webView.onResume()
        }
    }

    /**
     * Responsible for setting up the WebView that shows the URL.
     * It also deals with URLs that are opened withing the WebView.
     */
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        // get webView settings that come from options
        webView.settings.builtInZoomControls = options.allowZoom
        webView.settings.mediaPlaybackRequiresUserGesture = options.mediaPlaybackRequiresUserAction

        // setup WebViewClient and WebChromeClient
        webView.webViewClient = customWebViewClient()
        webView.webChromeClient = customWebChromeClient()
    }

    /**
     * Use WebViewClient to handle events on the WebView
     */
    private fun customWebViewClient(): WebViewClient {

        val webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                if (isFirstLoad) {
                    sendWebViewEvent(OSIABEvents.BrowserPageLoaded)
                    isFirstLoad = false
                }
                // store cookies after page finishes loading
                storeCookies()
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val urlString = request?.url.toString()
                return when {
                    // handle tel: links opening the appropriate app
                    urlString.startsWith("tel:") -> {
                        launchIntent(Intent.ACTION_DIAL, urlString)
                    }
                    // handle sms: and mailto: links opening the appropriate app
                    urlString.startsWith("sms:") || urlString.startsWith("mailto:") -> {
                        launchIntent(Intent.ACTION_SENDTO, urlString)
                    }
                    // handle geo: links opening the appropriate app
                    urlString.startsWith("geo:") -> {
                        launchIntent(Intent.ACTION_VIEW, urlString)
                    }
                    // handle Google Play Store links opening the appropriate app
                    urlString.startsWith("https://play.google.com/store") || urlString.startsWith("market:") -> {
                        launchIntent(Intent.ACTION_VIEW, urlString, true)
                    }
                    // handle every http and https link by loading it in the WebView
                    urlString.startsWith("http:") || urlString.startsWith("https:") -> {
                        view?.loadUrl(urlString)
                        urlText.text = urlString
                        true
                    }
                    else -> false
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                // show the default WebView error page
                super.onReceivedError(view, request, error)
            }

            /**
             * Responsible for handling and launching intents based on a URL.
             * @param intentAction Action for the intent
             * @param urlString URL to be processed
             * @param isGooglePlayStore to determine if the URL is a Google Play Store link
             */
            private fun launchIntent(intentAction: String, urlString: String, isGooglePlayStore: Boolean = false): Boolean {
                val intent = Intent(intentAction).apply {
                    data = Uri.parse(urlString)
                    if (isGooglePlayStore) {
                        setPackage("com.android.vending")
                    }
                }
                startActivity(intent)
                return true
            }
        }
        return webViewClient
    }

    /**
     * Use WebChromeClient to handle JS events
     */
    private fun customWebChromeClient(): WebChromeClient {

        val webChromeClient = object : WebChromeClient() {

            // override any methods necessary

            // handle standard permissions (e.g. audio, camera)
            override fun onPermissionRequest(request: PermissionRequest?) {
                //super.onPermissionRequest(request)
                request?.let {
                    handlePermissionRequest(it)
                }
            }

            // specifically handle geolocation permission
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                //super.onGeolocationPermissionsShowPrompt(origin, callback)
                if (origin != null && callback != null) {
                    handleGeolocationPermission(origin, callback)
                }
            }

            // file chooser stuff, maybe we also have to do this
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                 return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            }

        }
        return webChromeClient
    }

    /**
     * Handle the back button press
     */
    override fun onBackPressed() {
        if (options.hardwareBack && webView.canGoBack()) {
            webView.goBack()
        } else {
            sendWebViewEvent(OSIABEvents.BrowserFinished)
            webView.destroy()
            super.onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Handle permission requests
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STANDARD_PERMISSION -> {
                val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                currentPermissionRequest?.let {
                    if (granted) {
                        it.grant(it.resources)
                    } else {
                        it.deny()
                    }
                }
                currentPermissionRequest = null
            }
            REQUEST_LOCATION_PERMISSION -> {
                val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                geolocationCallback?.invoke(geolocationOrigin, granted, false)
                geolocationCallback = null
                geolocationOrigin = null
            }
        }
    }

    /**
     * Clears the WebView cache and removes all cookies if 'clearCache' parameter is 'true'.
     * If not, then if 'clearSessionCache' is true, removes the session cookies.
     */
    private fun possiblyClearCacheOrSessionCookies() {
        if (options.clearCache) {
            webView.clearCache(true)
            CookieManager.getInstance().removeAllCookies(null)
        } else if (options.clearSessionCache) {
            CookieManager.getInstance().removeSessionCookies(null)
        }
    }

    /**
     * Enables third party cookies using the CookieManager
     */
    private fun enableThirdPartyCookies() {
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
    }

    /**
     * Stores cookies using the CookieManager
     */
    private fun storeCookies() {
        CookieManager.getInstance().flush()
    }

    /**
     * Responsible for sending broadcasts.
     * @param event String identifying the event to send in the broadcast.
     */
    private fun sendWebViewEvent(event: OSIABEvents) {
        lifecycleScope.launch {
             OSIABEvents.browserEvents.emit(event)
        }
    }

    /**
     * Responsible for handling standard permission requests coming from the WebView
     * @param request PermissionRequest containing the permissions to request
     */
    private fun handlePermissionRequest(request: PermissionRequest) {
        val permissions = request.resources
        val permissionsNeeded = mutableListOf<String>()

        if (permissions.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.CAMERA)
            }
        }

        if (permissions.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_STANDARD_PERMISSION)
            currentPermissionRequest = request
        } else {
            request.grant(request.resources)
        }
    }

    /**
     *
     */
    private fun handleGeolocationPermission(origin: String, callback: GeolocationPermissions.Callback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            geolocationCallback = callback
            geolocationOrigin = origin
        } else {
            callback.invoke(origin, true, false)
        }
    }

}