package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import android.os.Bundle
import androidx.browser.customtabs.CustomTabsCallback

class OSIABCustomTabsCallback(private val onEventReceived: (Int) -> Unit): CustomTabsCallback() {
    override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
        super.onNavigationEvent(navigationEvent, extras)
        onEventReceived(navigationEvent)
    }
}