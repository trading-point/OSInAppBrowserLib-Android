package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers.OSIABRouterSpy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OSIABEngineTests {
    private val url = "https://www.outsystems.com/"
    private lateinit var customTabsRouterSpy : OSIABRouterSpy
    private lateinit var externalBrowserRouterSpy : OSIABRouterSpy
    private lateinit var webViewRouterSpy : OSIABRouterSpy

    @Test
    fun test_open_externalBrowserWithoutIssues_doesOpenBrowser() {
        makeSUT(true).openExternalBrowser(externalBrowserRouterSpy, url) { result ->
            assertTrue(result)
        }
    }

    @Test
    fun test_open_externalBrowserWithIssues_doesNotOpenBrowser() {
        makeSUT(false).openExternalBrowser(externalBrowserRouterSpy, url) { result ->
            assertFalse(result)
        }
    }
    @Test
    fun test_open_webViewWithoutIssues_doesOpenWebView() {
        makeSUT(true).openWebView(webViewRouterSpy, url) { result ->
            assertTrue(result)
        }
    }

    @Test
    fun test_open_webViewWithIssues_doesNotOpenWebView() {
        makeSUT(false).openWebView(webViewRouterSpy, url) { result ->
            assertFalse(result)
        }
    }

    @Test
    fun test_open_customTabsWithoutIssues_doesOpenCustomTabs() {
        makeSUT(true).openCustomTabs(customTabsRouterSpy, url) { result ->
            assertTrue(result)
        }
    }

    @Test
    fun test_open_customTabsWithIssues_doesNotOpenCustomTabs() {
        makeSUT(false).openCustomTabs(customTabsRouterSpy, url) { result ->
            assertFalse(result)
        }
    }

    private fun makeSUT(shouldOpenBrowser: Boolean): OSIABEngine {
        customTabsRouterSpy = OSIABRouterSpy(shouldOpenBrowser)
        externalBrowserRouterSpy = OSIABRouterSpy(shouldOpenBrowser)
        webViewRouterSpy = OSIABRouterSpy(shouldOpenBrowser)
        return OSIABEngine()
    }
}
