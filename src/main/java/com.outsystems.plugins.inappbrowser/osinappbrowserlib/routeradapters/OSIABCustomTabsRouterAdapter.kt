package com.outsystems.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.OSIABRouter
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.canOpenURL
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers.OSIABCustomTabsSessionHelper
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.helpers.OSIABCustomTabsSessionHelperInterface
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABAnimation
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABCustomTabsOptions
import com.outsystems.plugins.inappbrowser.osinappbrowserlib.models.OSIABViewStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OSIABCustomTabsRouterAdapter(
    private val context: Context,
    private val lifecycleScope: CoroutineScope,
    private val customTabsSessionHelper: OSIABCustomTabsSessionHelperInterface =
        OSIABCustomTabsSessionHelper(),
    private val options: OSIABCustomTabsOptions
) : OSIABRouter<Boolean> {
    private var customTabsSession: CustomTabsSession? = null

    private fun buildCustomTabsIntent(): CustomTabsIntent {
        val builder = CustomTabsIntent.Builder(customTabsSession)
        
        builder.setShowTitle(options.showTitle)
        builder.setUrlBarHidingEnabled(options.hideToolbarOnScroll)

        when (options.startAnimation) {
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

        when (options.exitAnimation) {
            OSIABAnimation.FADE_IN -> builder.setExitAnimations(
                context,
                android.R.anim.fade_out,
                android.R.anim.fade_in
            )

            OSIABAnimation.FADE_OUT -> builder.setExitAnimations(
                context,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

            OSIABAnimation.SLIDE_IN_LEFT -> builder.setExitAnimations(
                context,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )

            OSIABAnimation.SLIDE_OUT_RIGHT -> builder.setExitAnimations(
                context,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }

        if (options.viewStyle == OSIABViewStyle.BOTTOM_SHEET) {
            options.bottomSheetOptions?.let { bottomSheetOptions ->
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

        return builder.build()
    }

    override fun handleOpen(url: String, completionHandler: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val uri = Uri.parse(url)
                if (!context.canOpenURL(uri)) {
                    completionHandler(false)
                    return@launch
                }

                if (null == customTabsSession) {
                    customTabsSession = customTabsSessionHelper.generateNewCustomTabsSession(context)
                }

                val customTabsIntent = buildCustomTabsIntent()
                customTabsIntent.launchUrl(context, uri)
                completionHandler(true)
            } catch (e: Exception) {
                completionHandler(false)
            }
        }
    }
}
