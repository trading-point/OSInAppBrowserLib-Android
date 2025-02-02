package com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.routeradapters

import android.content.Context
import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.OSIABClosable
import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.OSIABRouter
import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.helpers.OSIABFlowHelperInterface
import com.tradingpoint.plugins.inappbrowser.osinappbrowserlib.models.OSIABOptions
import kotlinx.coroutines.CoroutineScope

abstract class OSIABBaseRouterAdapter<OptionsType : OSIABOptions, ReturnType>(
    protected val context: Context,
    protected val lifecycleScope: CoroutineScope,
    protected val options: OptionsType,
    protected val flowHelper: OSIABFlowHelperInterface,
    protected val onBrowserPageLoaded: () -> Unit,
    protected val onBrowserFinished: () -> Unit
) : OSIABRouter<ReturnType>, OSIABClosable {
    abstract override fun close(completionHandler: (Boolean) -> Unit)
    abstract override fun handleOpen(url: String, headers: Map<String, String>, completionHandler: (ReturnType) -> Unit)
}