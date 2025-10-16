package com.github.mtkw0127.scheduleshare.util

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
actual fun rememberMapUtil(): MapUtil {
    val context = LocalContext.current
    return remember {
        object : MapUtil {
            override fun openMap(location: String) {
                try {
                    // URLエンコードして検索クエリを作成
                    val encodedLocation = Uri.encode(location)
                    val uri = "geo:0,0?q=${Uri.encode(encodedLocation)}".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.resolveActivity(context.packageManager)?.let {
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
