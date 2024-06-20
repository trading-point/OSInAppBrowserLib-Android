package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions

class OSIABEngine(
    private val externalBrowserRouter: OSIABRouter<Unit, Boolean>,
    private val webViewRouter: OSIABRouter<OSIABWebViewOptions, Boolean>
) {

    /**
     * Trigger the external browser to open the passed `url`.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url using the External Browser.
     * @return Indicates if the operation was successful or not.
     */
    fun openExternalBrowser(url: String, completionHandler: (Boolean) -> Unit) {
        return externalBrowserRouter.handleOpen(url, null, null, completionHandler)
    }

    /**
     * Trigger the WebView to open the passed `url`.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url using the WebView.
     */
    fun openWebView(url: String, options: OSIABWebViewOptions? = null, callbackID: String?, completionHandler: (Boolean) -> Unit) {
        return webViewRouter.handleOpen(url, options, callbackID, completionHandler)
    }

}

fun Context.canOpenURL(uri: Uri): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    return intent.resolveActivity(packageManager) != null
}
