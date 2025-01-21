package com.outsystems.plugins.inappbrowser.osinappbrowserlib

interface OSIABRouter<ReturnType> {
    /**
     * Handles opening the passed `url`.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url.
     */
    fun handleOpen(url: String, headers: HashMap<String, String>, completionHandler: (ReturnType) -> Unit)
}