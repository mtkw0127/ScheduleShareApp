package com.github.mtkw0127.scheduleshare.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIApplication

@Composable
actual fun rememberMapUtil(): MapUtil {
    return remember {
        object : MapUtil {
            override fun openMap(location: String) {
                try {
                    // Apple Mapsで開くURL
                    val urlComponents = NSURLComponents()
                    urlComponents.scheme = "maps"
                    urlComponents.host = ""
                    urlComponents.queryItems = listOf(
                        NSURLQueryItem(name = "q", value = location)
                    )

                    val mapsUrl = urlComponents.URL

                    if (mapsUrl != null && UIApplication.sharedApplication.canOpenURL(mapsUrl)) {
                        UIApplication.sharedApplication.openURL(mapsUrl)
                    } else {
                        // Apple Mapsが開けない場合は、Google Mapsをブラウザで開く
                        val googleMapsUrl = NSURL.URLWithString("https://www.google.com/maps/search/?api=1&query=$location")
                        if (googleMapsUrl != null) {
                            UIApplication.sharedApplication.openURL(googleMapsUrl)
                        }
                    }
                } catch (e: Exception) {
                    println("Error opening map: ${e.message}")
                }
            }
        }
    }
}
