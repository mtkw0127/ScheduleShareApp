package com.github.mtkw0127.scheduleshare.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    val context = LocalContext.current
    return remember { ClipboardManager(context) }
}
