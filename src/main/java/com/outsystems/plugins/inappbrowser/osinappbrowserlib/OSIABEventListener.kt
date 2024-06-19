package com.outsystems.plugins.inappbrowser.osinappbrowserlib

interface OSIABEventListener {
    fun onBrowserFinished()
    fun onBrowserPageLoaded()
}