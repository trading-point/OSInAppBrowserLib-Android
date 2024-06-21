package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import android.content.Context
import android.content.Intent
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

    @Test
    fun test_handleOpen_notAbleToOpenIt_returnsFalse() {
        val context = mockContext(ableToOpenURL = false)
        val sut = OSIABWebViewRouterAdapter(context)
        sut.handleOpen(url) {
            assertFalse(it)
        }
    }

    @Test
    fun test_handleOpen_ableToOpenIt_returnsTrue() {
        val context = mockContext(ableToOpenURL = true)
        val sut = OSIABWebViewRouterAdapter(context)
        sut.handleOpen(url) {
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
