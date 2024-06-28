package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.canOpenURL
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABAnimation
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABCustomTabsOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABViewStyle
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

class OSIABCustomTabsRouterAdapter(
    private val context: Context,
    private val options: OSIABCustomTabsOptions? = null,
    private val onBrowserPageLoaded: () -> Unit,
    private val onBrowserFinished: () -> Unit
) : OSIABRouter<Boolean> {
    private var customTabsSession: CustomTabsSession? = null

    companion object {
        const val CHROME_PACKAGE_NAME = "com.android.chrome"
    }

    private fun getDefaultCustomTabsPackageName(): String {
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

    private fun initializeCustomTabsSession() {
        CustomTabsClient.bindCustomTabsService(context, getDefaultCustomTabsPackageName(), object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                client.warmup(0L)
                customTabsSession = client.newSession(CustomTabsCallbackImpl())
            }

            override fun onServiceDisconnected(name: ComponentName) {
                customTabsSession = null
            }
        })
    }

    private suspend fun waitForCustomTabsSession() {
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun handleOpen(url: String, completionHandler: (Boolean) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val uri = Uri.parse(url)
                if (!context.canOpenURL(uri)) {
                    completionHandler(false)
                    return@launch
                }

                if (null == customTabsSession) {
                    withTimeout(2000) {
                        initializeCustomTabsSession()
                        waitForCustomTabsSession()
                    }
                }

                val builder = CustomTabsIntent.Builder(customTabsSession)

                options?.let {
                    builder.setShowTitle(it.showTitle)
                    builder.setUrlBarHidingEnabled(it.hideToolbarOnScroll)

                    when (it.startAnimation) {
                        OSIABAnimation.FADE_IN -> builder.setStartAnimations(
                            context,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )

                        OSIABAnimation.FADE_OUT -> builder.setStartAnimations(
                            context,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in
                        )

                        OSIABAnimation.SLIDE_IN_LEFT -> builder.setStartAnimations(
                            context,
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )

                        OSIABAnimation.SLIDE_OUT_RIGHT -> builder.setStartAnimations(
                            context,
                            android.R.anim.slide_out_right,
                            android.R.anim.slide_in_left
                        )
                    }

                    when (it.exitAnimation) {
                        OSIABAnimation.FADE_IN -> builder.setExitAnimations(
                            context,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )

                        OSIABAnimation.FADE_OUT -> builder.setExitAnimations(
                            context,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in
                        )

                        OSIABAnimation.SLIDE_IN_LEFT -> builder.setExitAnimations(
                            context,
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )

                        OSIABAnimation.SLIDE_OUT_RIGHT -> builder.setExitAnimations(
                            context,
                            android.R.anim.slide_out_right,
                            android.R.anim.slide_in_left
                        )
                    }

                    if (it.viewStyle == OSIABViewStyle.BOTTOM_SHEET) {
                        it.bottomSheetOptions?.let { bottomSheetOptions ->
                            if (bottomSheetOptions.isFixed) {
                                builder.setInitialActivityHeightPx(
                                    bottomSheetOptions.height,
                                    CustomTabsIntent.ACTIVITY_HEIGHT_FIXED
                                )
                            } else {
                                builder.setInitialActivityHeightPx(bottomSheetOptions.height)
                            }
                        }
                    }
                }

                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, uri)
                completionHandler(true)
            } catch (e: Exception) {
                completionHandler(false)
            }
        }
    }

    private inner class CustomTabsCallbackImpl: CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            super.onNavigationEvent(navigationEvent, extras)
            when (navigationEvent) {
                NAVIGATION_FINISHED -> {
                    onBrowserPageLoaded()
                }
                TAB_HIDDEN -> {
                    onBrowserFinished()
                }
            }
        }
    }

}