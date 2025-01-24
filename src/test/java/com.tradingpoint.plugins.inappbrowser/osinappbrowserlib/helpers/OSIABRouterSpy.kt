package com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.helpers

import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.OSIABClosable
import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.OSIABRouter

class OSIABRouterSpy(
    private val shouldOpenBrowser: Boolean,
    private val shouldCloseBrowser: Boolean
) : OSIABRouter<Boolean>, OSIABClosable {
    override fun handleOpen(url: String, headers: Map<String, String>, completionHandler: (Boolean) -> Unit) {
        completionHandler(shouldOpenBrowser)
    }

    override fun close(completionHandler: (Boolean) -> Unit) {
        completionHandler(shouldCloseBrowser)
    }
}