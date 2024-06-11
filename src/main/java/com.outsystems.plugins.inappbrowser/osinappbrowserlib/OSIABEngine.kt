package com.outsystems.plugins.inappbrowser.osinappbrowserlib

class OSIABEngine(private val router: OSIABRouter<Boolean>) {

    /**
     * Trigger the external browser to open the passed `url`.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url using the External Browser.
     * @return Indicates if the operation was successful or not.
     */
    fun openExternalBrowser(url: String, completionHandler: (Boolean) -> Unit) {
        return router.handleOpen(url, completionHandler)
    }
}