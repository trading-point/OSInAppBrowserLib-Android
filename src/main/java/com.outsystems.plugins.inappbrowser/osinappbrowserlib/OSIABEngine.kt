package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import android.app.Activity
import android.content.Intent
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.views.OSIABWebViewActivityNew

class OSIABEngine(private val router: OSIABRouter<Boolean>) {

    companion object {
        const val OPEN_WEB_VIEW_REQUEST_CODE = 333
        const val WEB_VIEW_URL_EXTRA = "WEB_VIEW_URL_EXTRA"
        const val WEB_VIEW_OPTIONS_EXTRA = "WEB_VIEW_OPTIONS_EXTRA"
    }

    /**
     * Trigger the external browser to open the passed `url`.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url using the External Browser.
     * @return Indicates if the operation was successful or not.
     */
    fun openExternalBrowser(url: String, completionHandler: (Boolean) -> Unit) {
        return router.handleOpen(url, completionHandler)
    }

    /**
     * Trigger the WebView to open the passed `url`.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url using the WebView.
     */
    fun openWebView(activity: Activity, url: String, options: OSIABWebViewOptions, completionHandler: (Boolean) -> Unit) {
        activity.startActivityForResult(
            Intent(
                activity, OSIABWebViewActivityNew::class.java
            ).apply {
                putExtra(WEB_VIEW_URL_EXTRA, url)
                putExtra(WEB_VIEW_OPTIONS_EXTRA, options)
            }
            , OPEN_WEB_VIEW_REQUEST_CODE
        )
    }
}