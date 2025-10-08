package com.github.mtkw0127.scheduleshare.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    return remember { ClipboardManager() }
}
