package com.github.mtkw0127.scheduleshare.components

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * プラットフォーム固有の日付ピッカーダイアログ
 * @param initialDate 初期選択日
 * @param onDateSelected 日付が選択されたときのコールバック
 * @param onDismiss キャンセルされたときのコールバック
 */
@Composable
expect fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
)

/**
 * プラットフォーム固有の時刻ピッカーダイアログ
 * @param initialTime 初期選択時刻
 * @param onTimeSelected 時刻が選択されたときのコールバック
 * @param onDismiss キャンセルされたときのコールバック
 */
@Composable
expect fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
)
