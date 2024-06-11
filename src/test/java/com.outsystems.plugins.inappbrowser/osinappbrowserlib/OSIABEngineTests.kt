package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers.OSIABRouterSpy
import org.junit.Test

import org.junit.Assert.*

class OSIABEngineTests {
    private val url = "https://www.outsystems.com/"

    @Test
    fun test_open_externalBrowserWithoutIssues_doesOpenBrowser() {
        makeSUT(true).openExternalBrowser(url) { result ->
            assertTrue(result)
        }
    }

    @Test
    fun test_open_externalBrowserWithIssues_doesNotOpenBrowser() {
        makeSUT(false).openExternalBrowser(url) { result ->
            assertFalse(result)
        }
    }

    private fun makeSUT(shouldOpenBrowser: Boolean): OSIABEngine {
        return OSIABEngine(OSIABRouterSpy(shouldOpenBrowser))
    }
}