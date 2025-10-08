package com.github.mtkw0127.scheduleshare.util

import platform.UIKit.UIPasteboard

actual class ClipboardManager {
    actual fun setText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
