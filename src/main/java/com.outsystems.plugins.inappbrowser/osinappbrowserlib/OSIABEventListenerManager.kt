package com.outsystems.plugins.inappbrowser.osinappbrowserlib

interface OSIABEventListenerManager {

    fun addEventListener(listener: OSIABEventListener)

    fun removeAllEventListeners()

}