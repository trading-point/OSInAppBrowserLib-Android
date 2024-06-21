package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.Context
import android.content.Intent
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.views.OSIABWebViewActivity

class OSIABWebViewRouterAdapter(private val context: Context) : OSIABRouter<OSIABWebViewOptions, Boolean> {

    companion object {
        const val WEB_VIEW_URL_EXTRA = "WEB_VIEW_URL_EXTRA"
        const val WEB_VIEW_OPTIONS_EXTRA = "WEB_VIEW_OPTIONS_EXTRA"
    }
    override fun handleOpen(url: String, options: OSIABWebViewOptions?, completionHandler: (Boolean) -> Unit) {
        try {
            context.startActivity(
                Intent(
                    context, OSIABWebViewActivity::class.java
                ).apply {
                    putExtra(WEB_VIEW_URL_EXTRA, url)
                    putExtra(WEB_VIEW_OPTIONS_EXTRA, options)
                }
            )
            completionHandler(true)
        } catch (e: Exception) {
            completionHandler(false)
        }
    }
}