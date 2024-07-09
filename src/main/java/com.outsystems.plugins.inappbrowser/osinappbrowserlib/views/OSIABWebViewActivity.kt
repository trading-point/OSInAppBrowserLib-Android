package com.outsystems.plugins.inappbrowser.osinappbrowserlib.views

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABEvents
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.R
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABToolbarPosition
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import kotlinx.coroutines.launch

class OSIABWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var closeButton: TextView
    private lateinit var startNavigationButton: ImageButton
    private lateinit var endNavigationButton: ImageButton
    private lateinit var urlText: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var bottomToolbar: Toolbar
    private lateinit var errorView: View
    private lateinit var reloadButton: Button
    private lateinit var loadingView: View
    private lateinit var options: OSIABWebViewOptions
    private lateinit var appName: String

    // for the browserPageLoaded event, which we only want to trigger on the first URL loaded in the WebView
    private var isFirstLoad = true

    // for the error screen
    private var currentUrl: String? = null
    private var hasLoadError: Boolean = false

    companion object {
        const val WEB_VIEW_URL_EXTRA = "WEB_VIEW_URL_EXTRA"
        const val WEB_VIEW_OPTIONS_EXTRA = "WEB_VIEW_OPTIONS_EXTRA"
        const val DISABLED_ALPHA = 0.3f
        const val ENABLED_ALPHA = 1.0f
        val errorsToHandle = listOf(
            WebViewClient.ERROR_HOST_LOOKUP,
            WebViewClient.ERROR_UNSUPPORTED_SCHEME,
            WebViewClient.ERROR_BAD_URL
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appName = applicationInfo.loadLabel(packageManager).toString()

        // get parameters from intent extras
        val urlToOpen = intent.extras?.getString(WEB_VIEW_URL_EXTRA)
        options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getSerializable(
                WEB_VIEW_OPTIONS_EXTRA,
                OSIABWebViewOptions::class.java
            ) ?: OSIABWebViewOptions()
        } else {
            intent.extras?.getSerializable(WEB_VIEW_OPTIONS_EXTRA) as OSIABWebViewOptions
        }

        setContentView(R.layout.activity_web_view)

        //get elements in screen
        webView = findViewById(R.id.webview)
        errorView = findViewById(R.id.error_layout)
        reloadButton = createReloadButton()
        loadingView = findViewById(R.id.loading_layout)
        toolbar = findViewById(R.id.toolbar)
        bottomToolbar = findViewById(R.id.bottom_toolbar)
        
        closeButton = findViewById(R.id.close_button)
        closeButton.text = options.closeButtonText.ifBlank { "Close" }
        closeButton.setOnClickListener {
            sendWebViewEvent(OSIABEvents.BrowserFinished)
            webView.destroy()
            finish()
        }

        if (options.showToolbar)
            updateToolbar(
                options.toolbarPosition,
                options.showNavigationButtons,
                options.leftToRight,
                options.showURL,
                urlToOpen.orEmpty()
            )

        //we'll always have the top toolbar, because of the Close button
        toolbar.isVisible = options.showToolbar
        bottomToolbar.isVisible =
            options.showToolbar && options.toolbarPosition == OSIABToolbarPosition.BOTTOM
        // clear cache if necessary
        possiblyClearCacheOrSessionCookies()
        // enable third party cookies
        enableThirdPartyCookies()

        setupWebView()
        if (urlToOpen != null) {
            webView.loadUrl(urlToOpen)
            showLoadingScreen()
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
     * Helper function to update navigation button states
     */
    private fun updateNavigationButtons() {
        var startEnabled = webView.canGoBack()
        var endEnabled = webView.canGoForward()

        if (options.leftToRight) {
            startEnabled = webView.canGoForward()
            endEnabled = webView.canGoBack()
        }
        updateNavigationButton(
            startNavigationButton,
            startEnabled
        )
        updateNavigationButton(
            endNavigationButton,
            endEnabled
        )
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

        if (!options.customUserAgent.isNullOrEmpty())
            webView.settings.userAgentString = options.customUserAgent

        // get webView settings that come from options
        webView.settings.builtInZoomControls = options.allowZoom
        webView.settings.mediaPlaybackRequiresUserGesture = options.mediaPlaybackRequiresUserAction

        // setup WebViewClient and WebChromeClient
        webView.webViewClient =
            customWebViewClient(
                options.showNavigationButtons && options.showToolbar,
                options.showURL && options.showToolbar
            )
        webView.webChromeClient = customWebChromeClient()
    }

    /**
     * Use WebViewClient to handle events on the WebView
     */
    private fun customWebViewClient(
        hasNavigationButtons: Boolean,
        showURL: Boolean,
    ): WebViewClient {
        return OSIABWebViewClient(hasNavigationButtons, showURL)
    }

    /**
     * Use WebChromeClient to handle JS events
     */
    private fun customWebChromeClient(): WebChromeClient {

        val webChromeClient = object : WebChromeClient() {
            // override any methods necessary
        }
        return webChromeClient
    }

    /**
     * Handle the back button press
     */
    override fun onBackPressed() {
        if (options.hardwareBack && webView.canGoBack()) {
            hideErrorScreen()
            webView.goBack()
        } else {
            sendWebViewEvent(OSIABEvents.BrowserFinished)
            webView.destroy()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Inner class with implementation for WebViewClient
     */
    private inner class OSIABWebViewClient(
        val hasNavigationButtons: Boolean,
        val showURL: Boolean
    ) : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            hideLoadingScreen()
            if (!hasLoadError) {
                hideErrorScreen()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (isFirstLoad && !hasLoadError) {
                sendWebViewEvent(OSIABEvents.BrowserPageLoaded)
                isFirstLoad = false
            }

            // set back to false so that the next successful load
            // if the load fails, onReceivedError takes care of setting it back to true
            hasLoadError = false

            // store cookies after page finishes loading
            storeCookies()
            if (hasNavigationButtons) updateNavigationButtons()
            if (showURL) urlText.text = url
            currentUrl = url
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
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
                    if (showURL) urlText.text = urlString
                    true
                }

                else -> false
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            // let all errors first be handled by the WebView default error handling mechanism
            super.onReceivedError(view, request, error)

            // we only want to show the error screen for some errors (e.g. no internet)
            // e.g. we don't want to show it for an error where an image fails to load
            error?.let {
                if (errorsToHandle.contains(it.errorCode)) {
                    hasLoadError = true
                    showErrorScreen()
                }
            }
        }

        /**
         * Responsible for handling and launching intents based on a URL.
         * @param intentAction Action for the intent
         * @param urlString URL to be processed
         * @param isGooglePlayStore to determine if the URL is a Google Play Store link
         */
        private fun launchIntent(
            intentAction: String,
            urlString: String,
            isGooglePlayStore: Boolean = false
        ): Boolean {
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
     * Creates toolbar for the web view
     * @param toolbarPosition the toolbar position on screen
     * @param showNavigationButtons true, to show the back and forward buttons
     * @param isLeftRight true, to set the navigation buttons on the left
     * @param showURL true, to show the opened url

     */
    private fun updateToolbar(
        toolbarPosition: OSIABToolbarPosition,
        showNavigationButtons: Boolean,
        isLeftRight: Boolean,
        showURL: Boolean,
        url: String
    ) {
        var content: ConstraintLayout = toolbar.findViewById(R.id.toolbar_content)
        val navigationView: ConstraintLayout = content.findViewById(R.id.navigation_view)

        val nav: LinearLayout = findViewById(R.id.navigation_buttons)

        if (toolbarPosition == OSIABToolbarPosition.BOTTOM) {
            content.removeView(navigationView)
            content = bottomToolbar.findViewById(R.id.bottom_toolbar_content)
            content.addView(navigationView)

            // changing where the url text begins
            val set = ConstraintSet()
            set.clone(content)
            set.connect(
                R.id.navigation_view,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0
            )
            set.applyTo(content)
        }

        if (!showNavigationButtons) navigationView.removeView(nav)
        else defineNavigationButtons(isLeftRight, content)

        urlText = content.findViewById(R.id.url_text)
        if (!showURL) navigationView.removeView(urlText)
        else urlText.text = url


        if (isLeftRight) {
            bottomToolbar.layoutDirection = View.LAYOUT_DIRECTION_RTL
            toolbar.layoutDirection = View.LAYOUT_DIRECTION_RTL

        }
    }

    /**
     * Updates the navigation button variables and
     * defines their onClick listener
     * @param isLeftRight defines their placement, inside the toolbar
     * if <code>true</code>, start of the toolbar, else at the end
     * @param parent the view that contains the buttons
     */
    private fun defineNavigationButtons(isLeftRight: Boolean, parent: View) {
        startNavigationButton = parent.findViewById(R.id.start_button)
        endNavigationButton = parent.findViewById(R.id.end_button)
        startNavigationButton.setOnClickListener {
            if (!isLeftRight) backClick() else forwardClick()
        }

        endNavigationButton.setOnClickListener {
            if (!isLeftRight) forwardClick() else backClick()
        }
    }

    private fun forwardClick() {
        if (webView.canGoForward()) {
            webView.goForward()
        }
    }

    private fun backClick() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    /**
     * Helper function to apply styles based on enabled/disabled state
     * @param button the button that will be enabled / disabled
     * @param isEnabled whether to enabled or disable the button
     */
    private fun updateNavigationButton(button: ImageButton, isEnabled: Boolean) {
        button.isEnabled = isEnabled
        button.alpha = if (isEnabled) ENABLED_ALPHA else DISABLED_ALPHA
    }

    /**
     * Helper function to create the reload button
     * @return the Button after it has been created
     */
    private fun createReloadButton(): Button {
        return findViewById<Button?>(R.id.reload_button).apply {
            setOnClickListener {
                currentUrl?.let {
                    webView.loadUrl(it)
                    showLoadingScreen()
                }
            }
        }
    }

    /** Responsible for sending events using Kotlin Flows.
     * @param event String identifying the event to send.
     */
    private fun sendWebViewEvent(event: OSIABEvents) {
        lifecycleScope.launch {
            OSIABEvents.browserEvents.emit(event)
        }
    }

    private fun showErrorScreen() {
        webView.isVisible = false
        errorView.isVisible = true
        loadingView.isVisible = false
    }

    private fun hideErrorScreen() {
        errorView.isVisible = false
        webView.isVisible = true
    }

    private fun showLoadingScreen() {
        loadingView.isVisible = true
        errorView.isVisible = false
        webView.isVisible = false
    }

    private fun hideLoadingScreen() {
        loadingView.isVisible = false
        webView.isVisible = true
    }

}