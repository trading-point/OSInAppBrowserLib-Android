package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.mockito.Mockito.mock

class OSIABFlowHelperMock: OSIABFlowHelperInterface {

    var event: OSIABEvents = OSIABEvents.BrowserPageLoaded("")
    override fun listenToEvents(
        browserId: String,
        scope: CoroutineScope,
        onEventReceived: (OSIABEvents) -> Unit
    ): Job {
        onEventReceived(event)
        return mock(Job::class.java)
    }
}