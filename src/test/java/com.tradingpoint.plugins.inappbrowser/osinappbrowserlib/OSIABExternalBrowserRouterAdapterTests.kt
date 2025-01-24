package com.tradingpoint.plugins.inappbrowser.osinappbrowserlib

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.routeradapters.OSIABExternalBrowserRouterAdapter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OSIABExternalBrowserRouterAdapterTests {
    private val activityName = "TestActivity"
    private val packageName = "com.tradingpoint.plugins.inappbrowser.osinappbrowserlib"
    private val url = "https://www.tradingpoint.com/"
    private val headers = hashMapOf<String, String>()

    @Test
    fun test_handleOpen_withInvalidURL_returnsFalse() {
        val context = mockContext(useValidURL = false, ableToOpenURL = false)
        val sut = OSIABExternalBrowserRouterAdapter(context)
        sut.handleOpen(url, headers) { assertFalse(it) }
    }

    @Test
    fun test_handleOpen_withValidButNotAbleToOpenIt_returnsFalse() {
        val context = mockContext(useValidURL = true, ableToOpenURL = false)
        val sut = OSIABExternalBrowserRouterAdapter(context)
        sut.handleOpen(url, headers) { assertFalse(it) }
    }

    @Test
    fun test_handleOpen_withValidAndAbleToOpenIt_returnsTrue() {
        val context = mockContext(useValidURL = true, ableToOpenURL = true)
        val sut = OSIABExternalBrowserRouterAdapter(context)
        sut.handleOpen(url, headers) { assertTrue(it) }
    }

    private fun mockContext(useValidURL: Boolean, ableToOpenURL: Boolean = false): Context {
        val context = mock(Context::class.java)
        val packageManager = mock(android.content.pm.PackageManager::class.java)

        val resolveInfo = if (useValidURL) {
            val resolveInfo = ResolveInfo()
            val activityInfo = ActivityInfo()
            val applicationInfo = ApplicationInfo()

            applicationInfo.packageName = packageName

            activityInfo.applicationInfo = applicationInfo
            activityInfo.packageName = packageName
            activityInfo.name = activityName

            resolveInfo.activityInfo = activityInfo
            resolveInfo
        } else null

        `when`(context.packageManager).thenReturn(packageManager)
        `when`(packageManager.resolveActivity(any(Intent::class.java), anyInt())).thenReturn(resolveInfo)

        if (!ableToOpenURL) {
            doThrow(RuntimeException("Unable to open URL")).`when`(context).startActivity(any(Intent::class.java))
        }

        return context
    }
}
