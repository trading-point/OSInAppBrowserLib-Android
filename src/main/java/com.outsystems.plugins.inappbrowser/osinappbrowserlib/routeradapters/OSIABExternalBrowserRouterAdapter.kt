package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser.EXTRA_APPLICATION_ID
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter

class OSIABExternalBrowserRouterAdapter(private val context: Context) : OSIABRouter<Boolean> {
    override fun handleOpen(url: String, headers: HashMap<String, String>,completionHandler: (Boolean) -> Unit) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // TODO add headers
            // Bundle bundle = new Bundle();
            // bundle.putString("iv-user", username);
            // browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);

            intent.putExtra(EXTRA_APPLICATION_ID, context.packageName)
            context.startActivity(intent)
            completionHandler(true)
        } catch (e: Exception) {
            completionHandler(false)
        }
    }
}