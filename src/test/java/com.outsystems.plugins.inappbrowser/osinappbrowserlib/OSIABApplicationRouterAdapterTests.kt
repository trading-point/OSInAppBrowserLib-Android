package com.outsystems.plugins.inappbrowser.osinappbrowserlib

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers.OSApplicationStub
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters.OSIABApplicationRouterAdapter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OSIABApplicationRouterAdapterTests {
    private val url = "https://www.outsystems.com/"

    @Test
    fun test_handleOpen_withInvalidURL_returnsFalse() {
        val sut = makeSUT(useValidURL = false)
        sut.handleOpen(url) { assertFalse(it) }
    }

    @Test
    fun test_handleOpen_withValidButNotAbleToOpenIt_returnsFalse() {
        val sut = makeSUT(useValidURL = true, ableToOpenURL = false)
        sut.handleOpen(url) { assertFalse(it) }
    }

    @Test
    fun test_handleOpen_withValidAndAbleToOpenIt_returnsTrue() {
        val sut = makeSUT(useValidURL = true, ableToOpenURL = true)
        sut.handleOpen(url) { assertTrue(it) }
    }

    private fun makeSUT(useValidURL: Boolean, ableToOpenURL: Boolean = false): OSIABApplicationRouterAdapter {
        return OSIABApplicationRouterAdapter(OSApplicationStub(useValidURL, ableToOpenURL))
    }
}
