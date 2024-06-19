package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

interface OSIABEventListener {
    fun onBrowserFinished()
    fun onBrowserPageLoaded()
}