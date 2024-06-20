package com.outsystems.plugins.inappbrowser.osinappbrowserlib

interface OSIABEventListener {
    fun onBrowserFinished(callbackID: String?)
    fun onBrowserPageLoaded(callbackID: String?)
}