package com.outsystems.plugins.inappbrowser.osinappbrowserlib.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.views.ui.theme.InAppBrowserSampleAppTheme

class OSIABWebViewActivity : ComponentActivity() {

    companion object {
        const val WEB_VIEW_URL_EXTRA = "WEB_VIEW_URL_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val urlToOpen = intent.extras?.getString(WEB_VIEW_URL_EXTRA)

        setContent {
            InAppBrowserSampleAppTheme {
                if (!urlToOpen.isNullOrEmpty()) {
                    WebViewScreen(
                        url = urlToOpen
                    )
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(url: String) {

    var webView: WebView? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        AndroidView(
            factory = { context ->
                WebView(context).apply {

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    // Enable debugging for the WebView
                    WebView.setWebContentsDebuggingEnabled(true)

                    webViewClient = object : WebViewClient() {

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val urlString = request?.url.toString()
                            return when {
                                urlString.startsWith("tel:") -> {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse(urlString)
                                    }
                                    context.startActivity(intent)
                                    true
                                }
                                urlString.startsWith("sms:") -> {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse(urlString)
                                    }
                                    context.startActivity(intent)
                                    true
                                }
                                urlString.startsWith("mailto:") -> {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse(urlString)
                                    }
                                    context.startActivity(intent)
                                    true
                                }
                                urlString.startsWith("geo:") || urlString.startsWith("https://maps.google") ||
                                        urlString.startsWith("https://www.google.com/maps") || urlString.startsWith("https://google.com/maps") -> {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(urlString)
                                    }
                                    context.startActivity(intent)
                                    true
                                }
                                urlString.startsWith("https://play.google.com/store") || urlString.startsWith("market:") -> {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(urlString)
                                        setPackage("com.android.vending")
                                    }
                                    context.startActivity(intent)
                                    true
                                }
                                urlString.startsWith("http:") || urlString.startsWith("https:") -> {
                                    view?.loadUrl(urlString)
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
                            val errorCode = error?.errorCode
                            val errorDescription = error?.description
                        }
                    }
                    settings.javaScriptEnabled = true
                    //settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    //settings.setGeolocationEnabled(true)

                    loadUrl(url)
                }.also { webView = it  }
            },
            modifier = Modifier.weight(1f)
            /*
            update = { webView ->
                webView.loadUrl(url)
            }

             */
        )
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InAppBrowserSampleAppTheme {
        Greeting("Android")
    }
}