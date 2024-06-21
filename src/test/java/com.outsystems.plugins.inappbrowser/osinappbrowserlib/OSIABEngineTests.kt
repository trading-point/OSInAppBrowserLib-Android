package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers.OSIABRouterSpy
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import org.junit.Test

import org.junit.Assert.*

class OSIABEngineTests {
    private val url = "https://www.outsystems.com/"
    private val options = OSIABWebViewOptions()
    private val exampleCallbackID = "someCallbackID"

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

    @Test
    fun test_open_webViewWithoutIssues_doesOpenWebView() {
        makeSUT(true).openWebView(url) { result ->
            assertTrue(result)
        }
    }

    @Test
    fun test_open_webViewWithIssues_doesNotOpenWebView() {
        makeSUT(false).openWebView(url) { result ->
            assertFalse(result)
        }
    }

    @Test
    fun test_open_webViewWithOptionsAndCallbackWithoutIssues_doesOpenWebView() {
        makeSUT(true).openWebView(url, options, exampleCallbackID) { result ->
            assertTrue(result)
        }
    }

    private fun makeSUT(shouldOpenBrowser: Boolean): OSIABEngine {
        val externalBrowserRouterSpy = OSIABRouterSpy<Unit>(shouldOpenBrowser)
        val webViewRouterSpy = OSIABRouterSpy<OSIABWebViewOptions>(shouldOpenBrowser)
        return OSIABEngine(externalBrowserRouterSpy, webViewRouterSpy)
    }
}