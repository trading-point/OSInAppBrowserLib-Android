package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers.OSIABRouterSpy
import org.junit.Test

import org.junit.Assert.*

class OSIABEngineTests {
    @Test
    fun test_open_externalBrowserWithoutIssues_doesOpenBrowser() {
        val url = "https://www.outsystems.com/"
        assertTrue(makeSUT(shouldOpenBrowser = true).openExternalBrowser(url))
    }

    @Test
    fun test_open_externalBrowserWithIssues_doesNotOpenBrowser() {
        val url = "https://www.outsystems.com/"
        assertFalse(makeSUT(shouldOpenBrowser = false).openExternalBrowser(url))
    }

    private fun makeSUT(shouldOpenBrowser: Boolean): OSIABEngine {
        return OSIABEngine(OSIABRouterSpy(shouldOpenBrowser))
    }
}