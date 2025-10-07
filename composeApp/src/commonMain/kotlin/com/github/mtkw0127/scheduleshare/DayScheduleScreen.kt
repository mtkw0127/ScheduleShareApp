package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import com.github.mtkw0127.scheduleshare.extension.toJapanese
import com.github.mtkw0127.scheduleshare.extension.toYmd
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back
import scheduleshare.composeapp.generated.resources.plus
import kotlin.math.absoluteValue

@Composable
fun DayScheduleScreen(
    date: LocalDate,
    scheduleRepository: ScheduleRepository,
    onBackClick: () -> Unit,
    onDateChange: (LocalDate) -> Unit = {},
    onAddScheduleClick: () -> Unit = {}
) {
    var currentDate by remember(date) { mutableStateOf(date) }
    var dragOffset by remember { mutableStateOf(0f) }
    val schedules = remember(currentDate) {
        scheduleRepository.getSchedulesByDate(currentDate)
    }

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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddScheduleClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.plus),
                    contentDescription = "予定追加",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
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
            // 終日の予定を最初に表示
            val allDaySchedules = schedules.filter { it.timeType is Schedule.TimeType.AllDay }
            if (allDaySchedules.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "終日",
                        fontSize = 12.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    allDaySchedules.forEach { schedule ->
                        ScheduleCard(schedule)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // 時間軸と予定を表示
            val timedSchedules = schedules.filter { it.timeType is Schedule.TimeType.Timed }
            TimelineView(timedSchedules = timedSchedules)
        }
    }
}

@Composable
private fun TimelineView(timedSchedules: List<Schedule>) {
    val hourHeight = 60.dp

    Box(modifier = Modifier.fillMaxWidth()) {
        // 時間軸の背景（0-23時）
        Column {
            (0..23).forEach { hour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight)
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

                    // スケジュール配置用のスペース
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }

        // 予定を絶対配置
        Layout(
            content = {
                timedSchedules.forEach { schedule ->
                    when (val timeType = schedule.timeType) {
                        is Schedule.TimeType.Timed -> {
                            ScheduleCard(schedule)
                        }
                        else -> { /* 終日は別で表示済み */ }
                    }
                }
            },
            modifier = Modifier.padding(start = 60.dp)
        ) { measurables, constraints ->
            val hourHeightPx = hourHeight.toPx()

            val placeables = measurables.mapIndexed { index, measurable ->
                val schedule = timedSchedules[index]
                val timeType = schedule.timeType as Schedule.TimeType.Timed

                // 開始時刻から終了時刻までの分数を計算
                val startMinutes = timeType.start.hour * 60 + timeType.start.minute
                val endMinutes = timeType.end.hour * 60 + timeType.end.minute
                val durationMinutes = endMinutes - startMinutes

                // 高さを計算（分単位で）
                val scheduleHeight = (durationMinutes / 60f * hourHeightPx).toInt()

                // 幅は親の幅いっぱい、高さは計算した値
                val placeable = measurable.measure(
                    Constraints.fixed(
                        width = constraints.maxWidth - 16, // 左右のパディング
                        height = scheduleHeight.coerceAtLeast(40) // 最小40px
                    )
                )

                Triple(placeable, startMinutes, schedule)
            }

            // 24時間分の高さを計算
            val totalHeight = (24 * hourHeightPx).toInt()

            layout(constraints.maxWidth, totalHeight) {
                placeables.forEach { (placeable, startMinutes, _) ->
                    // Y座標を計算（開始時刻に基づく）
                    val yOffset = (startMinutes / 60f * hourHeightPx).toInt()

                    placeable.placeRelative(
                        x = 8, // 左パディング
                        y = yOffset
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(schedule: Schedule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = schedule.title,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (schedule.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = schedule.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            when (val timeType = schedule.timeType) {
                is Schedule.TimeType.Timed -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${timeType.start} - ${timeType.end}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                is Schedule.TimeType.AllDay -> { /* 終日は時刻を表示しない */ }
            }
        }
    }
}

@Composable
private fun HourlyScheduleItem(hour: Int, schedules: List<Schedule>) {
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

        // 予定表示部分
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                schedules.forEach { schedule ->
                    ScheduleCard(schedule)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
