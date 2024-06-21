package com.outsystems.plugins.inappbrowser.osinappbrowserlib.managers

interface OSIABEventManager {
    /**
     * Calls onBrowserPageLoaded() method of OSIABEventListener
     */
    fun notifyBrowserPageLoaded(callbackID: String?)

    /**
     * Calls onBrowserFinished() method of OSIABEventListener
     */
    fun notifyBrowserFinished(callbackID: String?)
}