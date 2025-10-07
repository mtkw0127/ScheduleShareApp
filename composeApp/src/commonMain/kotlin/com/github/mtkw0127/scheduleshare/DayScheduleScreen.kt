package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onAddScheduleClick: () -> Unit = {},
    onScheduleClick: (Schedule) -> Unit = {}
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
                        ScheduleCard(schedule, onClick = { onScheduleClick(schedule) })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // 時間軸と予定を表示
            val timedSchedules = schedules.filter { it.timeType is Schedule.TimeType.Timed }
            TimelineView(timedSchedules = timedSchedules, onScheduleClick = onScheduleClick)
        }
    }
}

@Composable
private fun TimelineView(
    timedSchedules: List<Schedule>,
    onScheduleClick: (Schedule) -> Unit = {}
) {
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
                            ScheduleCard(schedule, onClick = { onScheduleClick(schedule) })
                        }

                        else -> { /* 終日は別で表示済み */
                        }
                    }
                }
            },
            modifier = Modifier.padding(start = 60.dp)
        ) { measurables, constraints ->
            val hourHeightPx = hourHeight.toPx()

            // 予定の時間範囲情報を保持
            data class ScheduleInfo(
                val startMinutes: Int,
                val endMinutes: Int,
                val column: Int,
                val totalColumns: Int
            )

            // 重なりを検出して列を割り当て
            val scheduleInfos = mutableListOf<ScheduleInfo>()
            timedSchedules.forEach { schedule ->
                val timeType = schedule.timeType as Schedule.TimeType.Timed
                val startMinutes = timeType.start.hour * 60 + timeType.start.minute
                val endMinutes = timeType.end.hour * 60 + timeType.end.minute

                // この予定と重なる予定を探す
                val overlapping = scheduleInfos.filter { info ->
                    !(endMinutes <= info.startMinutes || startMinutes >= info.endMinutes)
                }

                // 使用可能な列を見つける
                val usedColumns = overlapping.map { it.column }.toSet()
                val column =
                    (0 until (overlapping.size + 1)).firstOrNull { it !in usedColumns } ?: 0

                // この予定と重なる全ての予定の最大列数を更新
                val maxColumns = maxOf(column + 1, overlapping.maxOfOrNull { it.totalColumns } ?: 1)

                scheduleInfos.add(ScheduleInfo(startMinutes, endMinutes, column, maxColumns))

                // 重なっている予定の totalColumns を更新
                overlapping.forEach { info ->
                    val index = scheduleInfos.indexOf(info)
                    if (index >= 0) {
                        scheduleInfos[index] = info.copy(totalColumns = maxColumns)
                    }
                }
            }

            val placeables = measurables.mapIndexed { index, measurable ->
                val info = scheduleInfos[index]
                val durationMinutes = info.endMinutes - info.startMinutes

                // 高さを計算（分単位で）
                val scheduleHeight = (durationMinutes / 60f * hourHeightPx).toInt()

                // 幅を列数で分割
                val availableWidth = constraints.maxWidth - 16
                val scheduleWidth = availableWidth / info.totalColumns

                val placeable = measurable.measure(
                    Constraints.fixed(
                        width = scheduleWidth - 4, // 列間のマージン
                        height = scheduleHeight.coerceAtLeast(40) // 最小40px
                    )
                )

                Triple(placeable, info.startMinutes, info)
            }

            // 24時間分の高さを計算
            val totalHeight = (24 * hourHeightPx).toInt()

            layout(constraints.maxWidth, totalHeight) {
                placeables.forEach { (placeable, startMinutes, info) ->
                    // Y座標を計算（開始時刻に基づく）
                    val yOffset = (startMinutes / 60f * hourHeightPx).toInt()

                    // X座標を列に基づいて計算
                    val availableWidth = constraints.maxWidth - 16
                    val columnWidth = availableWidth / info.totalColumns
                    val xOffset = 8 + (columnWidth * info.column)

                    placeable.placeRelative(
                        x = xOffset,
                        y = yOffset
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(4.dp)
        ) {
            val availableHeight = maxHeight

            // 高さに応じて表示内容を調整
            when {
                // 60dp未満: タイトルのみ、1行に省略
                availableHeight < 60.dp -> {
                    Text(
                        text = schedule.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 60-100dp: タイトルと時刻
                availableHeight < 100.dp -> {
                    Column {
                        Text(
                            text = schedule.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        when (val timeType = schedule.timeType) {
                            is Schedule.TimeType.Timed -> {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${timeType.start} - ${timeType.end}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }

                            is Schedule.TimeType.AllDay -> {}
                        }
                    }
                }
                // 100dp以上: 全て表示
                else -> {
                    Column {
                        Text(
                            text = schedule.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (schedule.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
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

                            is Schedule.TimeType.AllDay -> {}
                        }
                    }
                }
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
