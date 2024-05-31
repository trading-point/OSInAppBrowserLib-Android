package com.outsystems.plugins.inappbrowser.osinappbrowserlib

class OSIABEngine(private val router: OSIABRouter) {

    /**
     * Trigger the external browser to open the passed `url`.
     * @param url URL to be opened.
     * @return Indicates if the operation was successful or not.
     */
    fun openExternalBrowser(url: String): Boolean {
        return router.openInBrowser(url)
    }
}