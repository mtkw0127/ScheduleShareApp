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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
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

                            backgroundColor = platform.UIKit.UIColor.whiteColor

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

                            backgroundColor = platform.UIKit.UIColor.whiteColor

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
    val instant = localDate.atTime(0, 0).toInstant(TimeZone.UTC)
    val timeInterval = instant.toEpochMilliseconds() / 1000.0
    return NSDate(timeInterval)
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun nsDateToLocalDate(nsDate: NSDate): LocalDate {
    val timeInterval = nsDate.timeIntervalSince1970
    val instant = Instant.fromEpochMilliseconds((timeInterval * 1000).toLong())
    return instant.toLocalDateTime(TimeZone.UTC).date
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun localTimeToNSDate(localTime: LocalTime): NSDate {
    // 基準日 (2000-01-01) に時刻を設定
    val referenceDate = LocalDate(2000, 1, 1)
    val instant = referenceDate.atTime(localTime).toInstant(TimeZone.UTC)
    val timeInterval = instant.toEpochMilliseconds() / 1000.0
    return NSDate(timeInterval)
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun nsDateToLocalTime(nsDate: NSDate): LocalTime {
    val timeInterval = nsDate.timeIntervalSince1970
    val instant = Instant.fromEpochMilliseconds((timeInterval * 1000).toLong())
    val dateTime = instant.toLocalDateTime(TimeZone.UTC)
    return dateTime.time
}
