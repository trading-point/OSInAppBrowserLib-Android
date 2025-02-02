package com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.provider.Browser.EXTRA_APPLICATION_ID
import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.OSIABRouter

class OSIABExternalBrowserRouterAdapter(private val context: Context) : OSIABRouter<Boolean> {
    override fun handleOpen(url: String, headers: Map<String, String>,completionHandler: (Boolean) -> Unit) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)

            val headersBundle = Bundle()
            headers.forEach { key, value ->
                headersBundle.putString(key, value)
            }
            intent.putExtra(Browser.EXTRA_HEADERS, headersBundle)

            intent.putExtra(EXTRA_APPLICATION_ID, context.packageName)
            context.startActivity(intent)
            completionHandler(true)
        } catch (e: Exception) {
            completionHandler(false)
        }
    }
}