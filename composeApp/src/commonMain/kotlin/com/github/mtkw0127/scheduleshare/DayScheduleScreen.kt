package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import com.github.mtkw0127.scheduleshare.extension.toJapanese
import com.github.mtkw0127.scheduleshare.extension.toYmd
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back
import kotlin.math.absoluteValue

@Composable
fun DayScheduleScreen(
    date: LocalDate,
    onBackClick: () -> Unit,
    onDateChange: (LocalDate) -> Unit = {}
) {
    var currentDate by remember(date) { mutableStateOf(date) }
    var dragOffset by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = {
                    Text(
                        text = "${currentDate.toYmd()} (${currentDate.dayOfWeek.toJapanese()})",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.arrow_back),
                            contentDescription = "戻る",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = 100f
                            if (dragOffset.absoluteValue > threshold) {
                                val newDate = if (dragOffset > 0) {
                                    // 右スワイプ: 前日へ
                                    currentDate.plus(DatePeriod(days = -1))
                                } else {
                                    // 左スワイプ: 翌日へ
                                    currentDate.plus(DatePeriod(days = 1))
                                }
                                currentDate = newDate
                                onDateChange(newDate)
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        }
                    )
                }
                .verticalScroll(rememberScrollState())
        ) {
            // 0時から23時まで24時間分
            (0..23).forEach { hour ->
                HourlyScheduleItem(hour = hour)
            }
        }
    }
}

@Composable
private fun HourlyScheduleItem(hour: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
    ) {
        // 時刻表示部分
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "${hour.toString().padStart(2, '0')}:00",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 予定表示部分（空白）
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            // 予定がある場合はここに表示
        }
    }
}
