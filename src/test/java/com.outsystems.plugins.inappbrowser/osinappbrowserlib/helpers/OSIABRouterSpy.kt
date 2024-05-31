package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter

class OSIABRouterSpy(private val shouldOpenBrowser: Boolean) : OSIABRouter {
    override fun openInBrowser(url: String): Boolean {
        return shouldOpenBrowser
    }
}