package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import android.content.Context
import androidx.browser.customtabs.CustomTabsSession

interface OSIABCustomTabsSessionHelperInterface {

    /**
     * Generates a new CustomTabsSession instance
     * @param context Context to use when initializing the CustomTabsSession
     * @param onEventReceived Callback to send the session events (e.g. navigation finished)
     */
    suspend fun generateNewCustomTabsSession(context: Context, onEventReceived: (Int) -> Unit): CustomTabsSession?
}