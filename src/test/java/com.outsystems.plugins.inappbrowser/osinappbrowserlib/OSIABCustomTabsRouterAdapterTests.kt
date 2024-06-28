package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABAnimation
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABBottomSheet
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABCustomTabsOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABViewStyle
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters.OSIABCustomTabsRouterAdapter
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OSIABCustomTabsRouterAdapterTests {
    private val activityName = "TestActivity"
    private val packageName = "com.outsystems.plugins.inappbrowser.osinappbrowserlib"
    private val uri = Uri.parse("https://www.outsystems.com/")

    @Test
    fun test_handleOpen_withValidURL_launchesCustomTab() {
        val context = mockContext(useValidURL = true, ableToOpenURL = true)
        val options = OSIABCustomTabsOptions(
            showTitle = true,
            hideToolbarOnScroll = true,
            startAnimation = OSIABAnimation.SLIDE_IN_LEFT,
            exitAnimation = OSIABAnimation.SLIDE_OUT_RIGHT,
            viewStyle = OSIABViewStyle.BOTTOM_SHEET,
            bottomSheetOptions = OSIABBottomSheet(height = 100, isFixed = true)
        )

        val sut = OSIABCustomTabsRouterAdapter(context, options, {}, {})

        sut.handleOpen(uri.toString()) { success ->
            assertTrue(success)
        }
    }


    @Test
    fun test_handleOpen_withInvalidURL_returnsFalse() {
        val context = mockContext(useValidURL = false, ableToOpenURL = false)
        val sut = OSIABCustomTabsRouterAdapter(context, options = null, {}, {})

        sut.handleOpen("invalid_url") { success ->
            assertFalse(success)
        }
    }


    @Test
    fun test_handleOpen_withValidURLButException_returnsFalse() {
        val context = mock(Context::class.java)
        val packageManager = mock(android.content.pm.PackageManager::class.java)
        `when`(context.packageManager).thenReturn(packageManager)

        val sut = OSIABCustomTabsRouterAdapter(context, options = null, {}, {})

        `when`(packageManager.resolveActivity(any(Intent::class.java), anyInt())).thenReturn(ResolveInfo())

        doThrow(RuntimeException("Exception")).`when`(context).startActivity(any(Intent::class.java))

        sut.handleOpen(uri.toString()) { success ->
            assertFalse(success)
        }
    }

    @Test
    fun test_handleOpen_withValidURL_launchesCustomTab_when_browserPageLoaded_then_browserPageLoadedTriggered() =
        runTest(
            StandardTestDispatcher()
        ) {
            val context = mockContext(useValidURL = true, ableToOpenURL = true)
            val options = OSIABCustomTabsOptions(
                showTitle = true,
                hideToolbarOnScroll = true,
                startAnimation = OSIABAnimation.SLIDE_IN_LEFT,
                exitAnimation = OSIABAnimation.SLIDE_OUT_RIGHT,
                viewStyle = OSIABViewStyle.BOTTOM_SHEET,
                bottomSheetOptions = OSIABBottomSheet(height = 100, isFixed = true)
            )

            val sut = OSIABCustomTabsRouterAdapter(context, options,
                onBrowserPageLoaded = {
                    assertTrue(true)
                },
                onBrowserFinished = {
                    fail()
                }
            )

            sut.handleOpen(uri.toString()) { success ->
                assertTrue(success)
            }
        }

    private fun mockContext(useValidURL: Boolean, ableToOpenURL: Boolean = false): Context {
        val context = mock(Context::class.java)
        val packageManager = mock(android.content.pm.PackageManager::class.java)

        val resolveInfo = if (useValidURL && ableToOpenURL) {
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

        return context
    }
}