package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABEventListener

interface OSIABEventListenerManager {

    fun addEventListener(listener: OSIABEventListener)

}