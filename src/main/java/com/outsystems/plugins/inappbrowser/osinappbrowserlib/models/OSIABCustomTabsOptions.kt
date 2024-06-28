package com.outsystems.plugins.inappbrowser.osinappbrowserlib.models

data class OSIABCustomTabsOptions(
    val showTitle: Boolean = true,
    val hideToolbarOnScroll: Boolean = false,
    val viewStyle: OSIABViewStyle = OSIABViewStyle.FULL_SCREEN,
    val bottomSheetOptions: OSIABBottomSheet?,
    val startAnimation: OSIABAnimation,
    val exitAnimation: OSIABAnimation
)