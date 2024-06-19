package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABEventListener
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABEventListenerManager
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABEvents
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.views.OSIABWebViewActivity

class OSIABWebViewRouterAdapter(private val context: Context) : OSIABRouter<OSIABWebViewOptions, Boolean>, OSIABEventListenerManager {

    private val eventListeners = mutableListOf<OSIABEventListener>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                OSIABEvents.ACTION_BROWSER_PAGE_LOADED -> notifyBrowserPageLoaded()
                OSIABEvents.ACTION_BROWSER_FINISHED -> notifyBrowserFinished()
            }
        }
    }

    init {
        val intentFilter = IntentFilter().apply {
            addAction(OSIABEvents.ACTION_BROWSER_PAGE_LOADED)
            addAction(OSIABEvents.ACTION_BROWSER_FINISHED)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(broadcastReceiver, intentFilter)
        }

    }


    companion object {
        const val WEB_VIEW_URL_EXTRA = "WEB_VIEW_URL_EXTRA"
        const val WEB_VIEW_OPTIONS_EXTRA = "WEB_VIEW_OPTIONS_EXTRA"
    }
    override fun handleOpen(url: String, options: OSIABWebViewOptions?, completionHandler: (Boolean) -> Unit) {
        try {
            context.startActivity(
                Intent(
                    context, OSIABWebViewActivity::class.java
                ).apply {
                    putExtra(WEB_VIEW_URL_EXTRA, url)
                    putExtra(WEB_VIEW_OPTIONS_EXTRA, options)
                }
            )
            completionHandler(true)
        } catch (e: Exception) {
            completionHandler(false)
        }
    }

    override fun addEventListener(listener: OSIABEventListener) {
        eventListeners.add(listener)
    }

    private fun notifyBrowserPageLoaded() {
        eventListeners.forEach { it.onBrowserPageLoaded() }
    }

    private fun notifyBrowserFinished() {
        eventListeners.forEach { it.onBrowserFinished() }
    }

}