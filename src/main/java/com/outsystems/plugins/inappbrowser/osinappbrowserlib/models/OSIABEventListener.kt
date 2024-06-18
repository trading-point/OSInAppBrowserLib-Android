package com.outsystems.plugins.inappbrowser.osinappbrowserlib.models

interface OSIABEventListener {
    fun onBrowserClosed()
    fun onBrowserPageLoaded()
}