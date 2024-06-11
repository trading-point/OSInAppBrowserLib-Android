package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter

/**
 * Interface to be implemented by the objects that can handle opening URLs.
 * This is implemented by the `Context` object that can be used as an External Browser.
 */
interface OSIABApplicationDelegate {
    fun canOpenURL(url: Uri): Boolean
    fun open(url: Uri, options: Map<String, Any> = emptyMap(), completionHandler: ((Boolean) -> Unit)? = null)
}

/**
 * Provide a default implementation that abstracts the options parameter.
 */
fun OSIABApplicationDelegate.open(url: Uri, completionHandler: ((Boolean) -> Unit)? = null) {
    this.open(url, emptyMap(), completionHandler)
}

/**
 * Adapter that makes the required calls so that an `OSIABApplicationDelegate` implementation can perform the External Browser routing.
 */
class OSIABApplicationRouterAdapter(private val application: OSIABApplicationDelegate) :
    OSIABRouter<Boolean> {

    override fun handleOpen(url: String, completionHandler: (Boolean) -> Unit) {
        val uri = Uri.parse(url)
        if (!application.canOpenURL(uri)) {
            completionHandler(false)
            return
        }
        application.open(uri, completionHandler = completionHandler)
    }
}

/**
 * Make `Context` conform to the `OSIABApplicationDelegate` interface.
 */

fun Context.canOpenURL(url: Uri): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, url)
    return intent.resolveActivity(packageManager) != null
}

fun Context.open(url: Uri, options: Map<String, Any>, completionHandler: ((Boolean) -> Unit)?) {
    val intent = Intent(Intent.ACTION_VIEW, url)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
        startActivity(intent)
        completionHandler?.invoke(true)
    } catch (e: Exception) {
        completionHandler?.invoke(false)
    }
}

class ApplicationContextAdapter(private val context: Context) : OSIABApplicationDelegate {
    override fun canOpenURL(url: Uri): Boolean {
        return context.canOpenURL(url)
    }

    override fun open(url: Uri, options: Map<String, Any>, completionHandler: ((Boolean) -> Unit)?) {
        context.open(url, options, completionHandler)
    }
}
