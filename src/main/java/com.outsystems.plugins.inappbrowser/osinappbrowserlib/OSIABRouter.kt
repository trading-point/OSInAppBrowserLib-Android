package com.outsystems.plugins.inappbrowser.osinappbrowserlib

interface OSIABRouter {
    /**
     * Opens the passed `url` in the default browser.
     * @param url URL to be opened.
     * @return Indicates if the operation was successful or not.
     */
    fun openInBrowser(url: String): Boolean
}