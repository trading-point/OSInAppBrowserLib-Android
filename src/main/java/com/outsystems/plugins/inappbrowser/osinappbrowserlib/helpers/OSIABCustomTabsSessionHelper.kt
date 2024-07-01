package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class OSIABCustomTabsSessionHelper : OSIABCustomTabsSessionHelperInterface {

    private var customTabsSession: CustomTabsSession? = null

    companion object {
        const val CHROME_PACKAGE_NAME = "com.android.chrome"
    }

    private fun getDefaultCustomTabsPackageName(context: Context): String {
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
        val resolvedActivityList = context.packageManager.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = mutableListOf<String>()

        for (info in resolvedActivityList) {
            val serviceIntent = Intent().apply {
                action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
                `package` = info.activityInfo.packageName
            }
            if (context.packageManager.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        return if (packagesSupportingCustomTabs.isNotEmpty()) {
            packagesSupportingCustomTabs[0]
        } else {
            CHROME_PACKAGE_NAME
        }
    }

    private fun initializeCustomTabsSession(context: Context, onEventReceived: (Int) -> Unit) {
        CustomTabsClient.bindCustomTabsService(
            context,
            getDefaultCustomTabsPackageName(context),
            object : CustomTabsServiceConnection() {
                override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                    client.warmup(0L)
                    customTabsSession = client.newSession(CustomTabsCallbackImpl { onEventReceived(it) })
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    customTabsSession = null
                }
            }
        )
    }

    private suspend fun waitForCustomTabsSessionToConnect() {
        return suspendCancellableCoroutine { continuation ->
            val handler = Handler(Looper.getMainLooper())
            val checkSessionRunnable = object : Runnable {
                override fun run() {
                    if (customTabsSession != null) {
                        continuation.resume(Unit)
                    } else {
                        handler.postDelayed(this, 100)
                    }
                }
            }
            handler.post(checkSessionRunnable)
            continuation.invokeOnCancellation {
                handler.removeCallbacks(checkSessionRunnable)
            }
        }
    }

    private inner class CustomTabsCallbackImpl(private val onEventReceived: (Int) -> Unit) :
        CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            super.onNavigationEvent(navigationEvent, extras)
            onEventReceived(navigationEvent)
        }
    }

    /**
     * Generates a new CustomTabsSession instance
     * @param context Context to use when initializing the CustomTabsSession
     * @param onEventReceived Callback to send the session events (e.g. navigation finished)
     */
    override suspend fun generateNewCustomTabsSession(context: Context, onEventReceived: (Int) -> Unit): CustomTabsSession? {
        customTabsSession = null

        withTimeoutOrNull(2000) {
            initializeCustomTabsSession(context) { onEventReceived(it) }
            waitForCustomTabsSessionToConnect()
        }

        return customTabsSession
    }

}