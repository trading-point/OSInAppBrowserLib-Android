package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABEventListener

interface OSIABRouter<OptionsType, ReturnType> {
    /**
     * Handles opening the passed `url`.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url.
     */
    fun handleOpen(url: String, options: OptionsType? = null, completionHandler: (ReturnType) -> Unit)

}