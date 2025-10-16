package com.github.mtkw0127.scheduleshare.util

import androidx.compose.runtime.Composable

/**
 * マップアプリを開くためのインターフェース
 */
interface MapUtil {
    /**
     * 指定された場所をマップアプリで開く
     * @param location 場所の名前や住所
     */
    fun openMap(location: String)
}

/**
 * プラットフォーム固有のMapUtilインスタンスを提供
 */
@Composable
expect fun rememberMapUtil(): MapUtil
