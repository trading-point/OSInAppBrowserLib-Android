package com.tradingpoint.plugins.inappbrowser.osinappbrowserlib

class OSIABEngine {
    /**
     * Trigger the external browser to open the passed `url`.
     * @param externalBrowserRouter Router responsible for handling the external browser opening logic.
     * @param url URL to be opened
     * @param headers http headers
     * @param completionHandler The callback with the result of opening the url using the External Browser.
     * @return Indicates if the operation was successful or not.
     */
    fun openExternalBrowser(
        externalBrowserRouter: OSIABRouter<Boolean>,
        url: String,
        headers: Map<String, String>,
        completionHandler: (Boolean) -> Unit
    ) {
        return externalBrowserRouter.handleOpen(url, headers, completionHandler)
    }

    /**
     * Trigger the Custom Tabs to open the passed `url`.
     * @param customTabsRouter Router responsible for handling the Custom Tabs (system browser) opening logic.
     * @param url URL to be opened
     * @param headers http headers
     * @param completionHandler The callback with the result of opening the url using Custom Tabs.
     * @return Indicates if the operation was successful or not.
     */
    fun openCustomTabs(
        customTabsRouter: OSIABRouter<Boolean>,
        url: String,
        headers: Map<String, String>,
        completionHandler: (Boolean) -> Unit
    ) {
        return customTabsRouter.handleOpen(url, headers, completionHandler)
    }

    /**
     * Trigger the WebView to open the passed `url`.
     * @param webViewRouter Router responsible for handling the WebView opening logic.
     * @param url URL to be opened.
     * @param headers http headers
     * @param completionHandler The callback with the result of opening the url using the WebView.
     */
    fun openWebView(
        webViewRouter: OSIABRouter<Boolean>,
        url: String,
        headers: Map<String, String>,
        completionHandler: (Boolean) -> Unit
    ) {
        return webViewRouter.handleOpen(url, headers, completionHandler)
    }
}