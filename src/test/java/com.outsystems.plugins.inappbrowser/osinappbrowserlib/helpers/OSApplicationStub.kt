package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import android.net.Uri
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters.OSIABApplicationDelegate

class OSApplicationStub(private val useValidURL: Boolean, private val ableToOpenURL: Boolean) :
    OSIABApplicationDelegate {
    override fun canOpenURL(url: Uri): Boolean {
        return useValidURL
    }

    override fun open(url: Uri, options: Map<String, Any>, completionHandler: ((Boolean) -> Unit)?) {
        completionHandler?.invoke(ableToOpenURL)
    }
}
