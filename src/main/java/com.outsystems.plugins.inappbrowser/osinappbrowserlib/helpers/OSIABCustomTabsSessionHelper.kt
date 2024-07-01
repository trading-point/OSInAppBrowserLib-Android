package com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

interface OSIABCustomTabsSessionHelperInterface {
    suspend fun generateNewCustomTabsSession(context: Context): CustomTabsSession?
}

class OSIABCustomTabsSessionHelper: OSIABCustomTabsSessionHelperInterface {
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

    private fun initializeCustomTabsSession(context: Context) {
        CustomTabsClient.bindCustomTabsService(
            context,
            getDefaultCustomTabsPackageName(context),
            object : CustomTabsServiceConnection() {
                override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                    client.warmup(0L)
                    customTabsSession = client.newSession(null)
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

    override suspend fun generateNewCustomTabsSession(context: Context): CustomTabsSession? {
        customTabsSession = null

        withTimeoutOrNull(2000) {
            initializeCustomTabsSession(context)
            waitForCustomTabsSessionToConnect()
        }

        return customTabsSession
    }
}