package com.outsystems.plugins.inappbrowser.osinappbrowserlib

interface OSIABEventListener {
    /**
     * Handles the onBrowserPageLoaded event
     */
    fun onBrowserPageLoaded(callbackID: String?)
    /**
     * Handles the onBrowserFinished event
     */
    fun onBrowserFinished(callbackID: String?)
}