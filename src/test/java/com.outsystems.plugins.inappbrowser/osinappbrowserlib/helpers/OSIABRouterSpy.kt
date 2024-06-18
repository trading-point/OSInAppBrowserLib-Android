package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter

class OSIABRouterSpy<OptionsType>(private val shouldOpenBrowser: Boolean) : OSIABRouter<OptionsType, Boolean> {
    override fun handleOpen(url: String, options: OptionsType?, completionHandler: (Boolean) -> Unit) {
        completionHandler(shouldOpenBrowser)
    }
}