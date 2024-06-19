package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters.OSIABEventListener

interface OSIABEventListenerManager {

    fun addEventListener(listener: OSIABEventListener)

}