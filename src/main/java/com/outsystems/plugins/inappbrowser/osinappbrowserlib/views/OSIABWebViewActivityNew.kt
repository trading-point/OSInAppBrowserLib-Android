package com.outsystems.plugins.inappbrowser.osinappbrowserlib.views

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.R

class OSIABWebViewActivityNew : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var closeButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var urlText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val urlToOpen = intent.extras?.getString(OSIABWebViewActivity.WEB_VIEW_URL_EXTRA)

        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.webview)
        closeButton = findViewById(R.id.close_button)
        backButton = findViewById(R.id.back_button)
        forwardButton = findViewById(R.id.forward_button)
        urlText = findViewById(R.id.url_text)

        setupWebView()

        if (urlToOpen != null) {
            webView.loadUrl(urlToOpen)
        }

        closeButton.setOnClickListener {
            webView.destroy()
            finish()
        }
        closeButton.text = "Close"

        backButton.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        forwardButton.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

        urlText.text = urlToOpen

    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        //webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        //webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Enable debugging for the WebView
        //WebView.setWebContentsDebuggingEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //Toast.makeText(this@OSIABWebViewActivityNew, "Page Loaded", Toast.LENGTH_SHORT).show()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val urlString = request?.url.toString()
                return when {
                    urlString.startsWith("tel:") -> {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse(urlString)
                        }
                        startActivity(intent)
                        true
                    }
                    urlString.startsWith("sms:") -> {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse(urlString)
                        }
                        startActivity(intent)
                        true
                    }
                    urlString.startsWith("geo:") -> {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(urlString)
                        }
                        startActivity(intent)
                        true
                    }
                    urlString.startsWith("mailto:") -> {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse(urlString)
                        }
                        startActivity(intent)
                        true
                    }
                    urlString.startsWith("https://play.google.com/store") || urlString.startsWith("market:") -> {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(urlString)
                            setPackage("com.android.vending")
                        }
                        startActivity(intent)
                        true
                    }
                    urlString.startsWith("http:") || urlString.startsWith("https:") -> {
                        view?.loadUrl(urlString)
                        true
                    }
                    else -> false
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                //Toast.makeText(this@OSIABWebViewActivityNew, "Failed to load the page", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up a WebChromeClient to handle JavaScript alerts, etc.
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.message()?.let {
                    //Toast.makeText(this@OSIABWebViewActivityNew, it, Toast.LENGTH_SHORT).show()
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            webView.destroy()
            super.onBackPressedDispatcher.onBackPressed()
        }
    }

}