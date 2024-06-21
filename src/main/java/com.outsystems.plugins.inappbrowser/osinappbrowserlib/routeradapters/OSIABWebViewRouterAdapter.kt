package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABEventListener
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.managers.OSIABEventManager
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABEvents
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABWebViewOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.views.OSIABWebViewActivity

class OSIABWebViewRouterAdapter(
    private val context: Context,
    private val listener: OSIABEventListener
) : OSIABRouter<OSIABWebViewOptions, Boolean>, OSIABEventManager {

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val callbackID = intent?.extras?.getString(CALLBACK_ID_EXTRA)
            when (intent?.action) {
                OSIABEvents.ACTION_BROWSER_PAGE_LOADED -> notifyBrowserPageLoaded(callbackID)
                OSIABEvents.ACTION_BROWSER_FINISHED -> notifyBrowserFinished(callbackID)
            }
        }
    }

    companion object {
        const val WEB_VIEW_URL_EXTRA = "WEB_VIEW_URL_EXTRA"
        const val WEB_VIEW_OPTIONS_EXTRA = "WEB_VIEW_OPTIONS_EXTRA"
        const val CALLBACK_ID_EXTRA = "CALLBACK_ID_EXTRA"
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

    /**
     * Handles opening the passed `url` in the WebView.
     * @param url URL to be opened.
     * @param completionHandler The callback with the result of opening the url.
     * @param options WebView options to open the WebView with.
     * @param callbackID optional ID associated to a call to the method.
     */
    override fun handleOpen(url: String, options: OSIABWebViewOptions?, callbackID: String?, completionHandler: (Boolean) -> Unit) {
        try {
            context.startActivity(
                Intent(
                    context, OSIABWebViewActivity::class.java
                ).apply {
                    putExtra(WEB_VIEW_URL_EXTRA, url)
                    putExtra(WEB_VIEW_OPTIONS_EXTRA, options)
                    callbackID?.let {
                        putExtra(CALLBACK_ID_EXTRA, callbackID)
                    }
                }
            )
            completionHandler(true)
        } catch (e: Exception) {
            completionHandler(false)
        }
    }

    /**
     * Calls onBrowserPageLoaded() method of OSIABEventListener
     */
    override fun notifyBrowserPageLoaded(callbackID: String?) {
        listener.onBrowserPageLoaded(callbackID)
    }

    /**
     * Calls onBrowserFinished() method of OSIABEventListener
     */
    override fun notifyBrowserFinished(callbackID: String?) {
        listener.onBrowserFinished(callbackID)
    }

}