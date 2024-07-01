package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import android.content.ComponentName
import android.content.Context
import androidx.browser.customtabs.CustomTabsSession

class OSIABCustomTabsSessionHelperMock: OSIABCustomTabsSessionHelperInterface {
    private val componentName = "OSIABTestComponent"

    override suspend fun generateNewCustomTabsSession(context: Context): CustomTabsSession {
        return CustomTabsSession.createMockSessionForTesting(ComponentName(context, componentName))
    }
}