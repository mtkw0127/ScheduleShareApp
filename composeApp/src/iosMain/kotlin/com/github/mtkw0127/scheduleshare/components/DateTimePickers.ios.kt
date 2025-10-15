package com.github.mtkw0127.scheduleshare.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import platform.Foundation.NSDate
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerRef = remember { mutableStateOf<UIDatePicker?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "日付を選択",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // UIDatePicker
                UIKitView(
                    factory = {
                        UIDatePicker().apply {
                            datePickerMode = UIDatePickerMode.UIDatePickerModeDate
                            preferredDatePickerStyle =
                                platform.UIKit.UIDatePickerStyle.UIDatePickerStyleWheels

                            // 初期日付を設定
                            date = localDateToNSDate(initialDate)

                            // テキスト色を常に黒色にする（ライトモードスタイルを強制）
                            overrideUserInterfaceStyle = platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight

                            // ピッカーのインスタンスを保存
                            pickerRef.value = this
                        }
                    },
                    background = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("キャンセル")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        // OKボタンが押されたときにピッカーから直接値を取得
                        pickerRef.value?.date?.let { nsDate ->
                            val selectedDate = nsDateToLocalDate(nsDate)
                            onDateSelected(selectedDate)
                            nsDate
                        }
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerRef = remember { mutableStateOf<UIDatePicker?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "時刻を選択",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // UIDatePicker (Time mode)
                UIKitView(
                    factory = {
                        UIDatePicker().apply {
                            datePickerMode = UIDatePickerMode.UIDatePickerModeTime
                            preferredDatePickerStyle =
                                platform.UIKit.UIDatePickerStyle.UIDatePickerStyleWheels

                            // 初期時刻を設定
                            date = localTimeToNSDate(initialTime)

                            // テキスト色を常に黒色にする（ライトモードスタイルを強制）
                            overrideUserInterfaceStyle = platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight

                            // ピッカーのインスタンスを保存
                            pickerRef.value = this
                        }
                    },
                    background = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("キャンセル")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        // OKボタンが押されたときにピッカーから直接値を取得
                        pickerRef.value?.date?.let { nsDate ->
                            val selectedTime = nsDateToLocalTime(nsDate)
                            onTimeSelected(selectedTime)
                        }
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

// Helper functions for converting between LocalDate/LocalTime and NSDate

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun localDateToNSDate(localDate: LocalDate): NSDate {
    // NSCalendarを使用してローカルタイムゾーンで日付を構築
    val calendar = platform.Foundation.NSCalendar.currentCalendar
    val components = platform.Foundation.NSDateComponents()
    components.year = localDate.year.toLong()
    components.month = (localDate.month.ordinal + 1).toLong()
    components.day = localDate.day.toLong()
    components.hour = 0L
    components.minute = 0L
    components.second = 0L
    return calendar.dateFromComponents(components) ?: NSDate()
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun nsDateToLocalDate(nsDate: NSDate): LocalDate {
    // NSCalendarを使用してローカルタイムゾーンで日付を取得
    val calendar = platform.Foundation.NSCalendar.currentCalendar
    val components = calendar.components(
        platform.Foundation.NSCalendarUnitYear or
        platform.Foundation.NSCalendarUnitMonth or
        platform.Foundation.NSCalendarUnitDay,
        nsDate
    )
    return LocalDate(
        year = components.year.toInt(),
        month = kotlinx.datetime.Month(components.month.toInt()),
        day = components.day.toInt()
    )
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun localTimeToNSDate(localTime: LocalTime): NSDate {
    // NSCalendarを使用してローカルタイムゾーンで日時を構築
    val calendar = platform.Foundation.NSCalendar.currentCalendar
    val components = platform.Foundation.NSDateComponents()
    components.hour = localTime.hour.toLong()
    components.minute = localTime.minute.toLong()
    components.second = localTime.second.toLong()
    // 現在の日付を使用
    val now = NSDate()
    val nowComponents = calendar.components(
        platform.Foundation.NSCalendarUnitYear or
        platform.Foundation.NSCalendarUnitMonth or
        platform.Foundation.NSCalendarUnitDay,
        now
    )
    components.year = nowComponents.year
    components.month = nowComponents.month
    components.day = nowComponents.day
    return calendar.dateFromComponents(components) ?: NSDate()
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun nsDateToLocalTime(nsDate: NSDate): LocalTime {
    // NSCalendarを使用してローカルタイムゾーンで時刻を取得
    val calendar = platform.Foundation.NSCalendar.currentCalendar
    val components = calendar.components(
        platform.Foundation.NSCalendarUnitHour or
        platform.Foundation.NSCalendarUnitMinute or
        platform.Foundation.NSCalendarUnitSecond,
        nsDate
    )
    return LocalTime(
        hour = components.hour.toInt(),
        minute = components.minute.toInt(),
        second = components.second.toInt()
    )
}
