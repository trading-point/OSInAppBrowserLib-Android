package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import android.content.Context
import android.content.Intent
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters.OSIABWebViewRouterAdapter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OSIABWebViewRouterAdapterTests {
    private val url = "https://www.outsystems.com/"
    private val options = OSIABWebViewOptions()
    private val exampleCallbackID = "someCallbackID"

    private val eventListener = object : OSIABEventListener {

        override fun onBrowserFinished(callbackID: String?) {
            assertEquals(exampleCallbackID, callbackID)
        }

        override fun onBrowserPageLoaded(callbackID: String?) {
            assertEquals(exampleCallbackID, callbackID)
        }

    }

    @Test
    fun test_handleOpen_notAbleToOpenIt_returnsFalse() {
        val context = mockContext(ableToOpenURL = false)
        val sut = OSIABWebViewRouterAdapter(context, eventListener)
        sut.handleOpen(url) {
            assertFalse(it)
        }
    }

    @Test
    fun test_handleOpen_ableToOpenIt_returnsTrue() {
        val context = mockContext(ableToOpenURL = true)
        val sut = OSIABWebViewRouterAdapter(context, eventListener)
        sut.handleOpen(url) {
            assertTrue(it)
        }
    }

    @Test
    fun test_handleOpen_ableToOpenIt_when_browserPageLoads_then_browserPageLoadedTriggered() {
        val context = mockContext(ableToOpenURL = true)
        val sut = OSIABWebViewRouterAdapter(context, eventListener)
        sut.handleOpen(url) {
            assertTrue(it)
        }
        sut.notifyBrowserPageLoaded(exampleCallbackID)
    }

    @Test
    fun test_handleOpen_ableToOpenIt_when_browserFinished_then_browserFinishedTriggered() {
        val context = mockContext(ableToOpenURL = true)
        val sut = OSIABWebViewRouterAdapter(context, eventListener)
        sut.handleOpen(url) {
            assertTrue(it)
        }
        sut.notifyBrowserFinished(exampleCallbackID)
    }

    @Test
    fun test_handleOpenWithOptionsAndCallback_when_ableToOpenIt_returnsTrue() {
        val context = mockContext(ableToOpenURL = true)
        val sut = OSIABWebViewRouterAdapter(context, eventListener)
        sut.handleOpen(url, options, exampleCallbackID) {
            assertTrue(it)
        }
    }

    private fun mockContext(ableToOpenURL: Boolean = false): Context {
        val context = mock(Context::class.java)
        if (!ableToOpenURL) {
            doThrow(RuntimeException("Unable to open URL")).`when`(context).startActivity(any(Intent::class.java))
        }
        return context
    }

}
