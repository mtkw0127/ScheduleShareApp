package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import com.github.mtkw0127.scheduleshare.components.TimeLabelsColumn
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.schedule.ScheduleTime
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.repository.UserRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun WeekScheduleScreen(
    date: LocalDate,
    scheduleRepository: ScheduleRepository,
    userRepository: UserRepository,
    onBackClick: () -> Unit,
    onScheduleClick: (Schedule) -> Unit = {}
) {
    // 指定された日付を含む週の月曜日を取得
    val startOfWeek = remember(date) {
        val dayOfWeek = date.dayOfWeek
        val daysFromMonday = (dayOfWeek.ordinal) // Monday is 0
        date.plus(DatePeriod(days = -daysFromMonday))
    }

    // 月曜日から日曜日までの7日間
    val weekDays = remember(startOfWeek) {
        (0..6).map { startOfWeek.plus(DatePeriod(days = it)) }
    }

    // 各日の予定を取得
    val schedulesByDate = remember(weekDays) {
        weekDays.associateWith { day ->
            scheduleRepository.getSchedulesByDate(day)
        }
    }

    // ユーザーの色を取得する関数
    val getUserColor: (User.Id) -> Color = { userId ->
        Color(userRepository.getUserColor(userId).value)
    }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = {
                    Text(
                        text = "週間予定 ${startOfWeek.month.number}/${startOfWeek.dayOfMonth} - ${weekDays.last().month.number}/${weekDays.last().day}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
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
        ) {
            // 横スクロール状態を共有
            val sharedHorizontalScrollState = rememberScrollState()

            // 日付ヘッダー（固定）
            Row(modifier = Modifier.fillMaxWidth()) {
                // 左側固定: 時刻表示エリアの幅を合わせる
                Spacer(modifier = Modifier.width(60.dp))

                // 右側スクロール: 日付
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(sharedHorizontalScrollState)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    weekDays.forEach { day ->
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .padding(horizontal = 2.dp)
                        ) {
                            val isToday = day == today
                            Text(
                                text = "${day.month.number}/${day.day} (${
                                    getDayOfWeekString(day.dayOfWeek)
                                })",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // スクロール可能なコンテンツ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 終日予定の最大数を計算（最低1つは表示）
                val maxAllDayCount = schedulesByDate.values.maxOfOrNull { daySchedules ->
                    daySchedules.count { it.isAllDay }
                }?.coerceAtLeast(1) ?: 1

                Row(modifier = Modifier.fillMaxWidth()) {
                    // 左側固定: 時刻表示エリア
                    Column(
                        modifier = Modifier.width(60.dp)
                    ) {
                        // 終日エリアの高さを合わせる
                        val allDayAreaHeight = 16.dp + 16.dp + 4.dp + (68.dp * maxAllDayCount)
                        Spacer(modifier = Modifier.height(allDayAreaHeight))

                        // 時刻ラベル
                        TimeLabelsColumn()
                    }

                    // 右側スクロール: 日付の列
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(sharedHorizontalScrollState)
                    ) {
                        weekDays.forEach { day ->
                            val daySchedules = schedulesByDate[day] ?: emptyList()

                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 2.dp)
                            ) {
                                // 終日の予定エリア
                                val allDaySchedules = daySchedules.filter { it.isAllDay }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (allDaySchedules.isNotEmpty()) {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "終日",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (allDaySchedules.isNotEmpty())
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    allDaySchedules.forEach { schedule ->
                                        Box(modifier = Modifier.height(64.dp)) {
                                            ScheduleCard(
                                                schedule = schedule,
                                                containerColor = getUserColor(schedule.user.id),
                                                onClick = { onScheduleClick(schedule) }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    // 不足分は空のスペースで埋める
                                    val emptyCount = maxAllDayCount - allDaySchedules.size
                                    repeat(emptyCount) {
                                        Spacer(modifier = Modifier.height(68.dp))
                                    }
                                }

                                // 時間軸と予定を表示
                                val timedSchedules = daySchedules.filter { it.isTimed }
                                WeekTimelineView(
                                    timedSchedules = timedSchedules,
                                    currentDate = day,
                                    onScheduleClick = onScheduleClick,
                                    getUserColor = getUserColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun WeekTimelineView(
    timedSchedules: List<Schedule>,
    currentDate: LocalDate,
    onScheduleClick: (Schedule) -> Unit,
    getUserColor: (User.Id) -> Color
) {
    val hourHeight = 60.dp

    // 今日かどうかチェック
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isToday = currentDate == today

    // 現在時刻を取得
    val now = if (isToday) {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    } else {
        null
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // 時間軸の背景
        Column {
            (0..23).forEach { hour ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight)
                        .background(
                            if (hour % 2 == 0)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else
                                Color.Transparent
                        )
                ) {
                    HorizontalDivider()
                }
            }
        }

        // 現在時刻の線
        if (now != null) {
            val currentMinutes = now.hour * 60 + now.minute
            val offsetY = (currentMinutes / 60f) * hourHeight.value
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = offsetY.dp)
                    .background(Color.Red)
            )
        }

        // 予定を配置
        Box(modifier = Modifier.fillMaxWidth()) {
            timedSchedules.forEach { schedule ->
                when (val time = schedule.time) {
                    is ScheduleTime.SingleTimeDay -> {
                        val startMinutes = time.startTime.hour * 60 + time.startTime.minute
                        val endMinutes = time.endTime.hour * 60 + time.endTime.minute
                        val durationMinutes = endMinutes - startMinutes

                        val startOffsetY = (startMinutes / 60f) * hourHeight.value
                        val scheduleHeight = (durationMinutes / 60f) * hourHeight.value

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(scheduleHeight.dp)
                                .offset(y = startOffsetY.dp)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            ScheduleCard(
                                schedule = schedule,
                                containerColor = getUserColor(schedule.user.id),
                                onClick = { onScheduleClick(schedule) }
                            )
                        }
                    }

                    is ScheduleTime.DateTimeRange -> {
                        // 連日予定の場合、この日の開始時刻と終了時刻を計算
                        val scheduleStartDate = time.start.date
                        val scheduleEndDate = time.end.date

                        val startMinutes = if (scheduleStartDate == currentDate) {
                            time.start.time.hour * 60 + time.start.time.minute
                        } else {
                            0 // 前日から続いている場合は0:00から
                        }

                        val endMinutes = if (scheduleEndDate == currentDate) {
                            time.end.time.hour * 60 + time.end.time.minute
                        } else {
                            24 * 60 // 翌日以降も続く場合は23:59まで
                        }

                        val durationMinutes = endMinutes - startMinutes
                        val startOffsetY = (startMinutes / 60f) * hourHeight.value
                        val scheduleHeight = (durationMinutes / 60f) * hourHeight.value

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(scheduleHeight.dp)
                                .offset(y = startOffsetY.dp)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            ScheduleCard(
                                schedule = schedule,
                                containerColor = getUserColor(schedule.user.id),
                                onClick = { onScheduleClick(schedule) }
                            )
                        }
                    }

                    else -> {} // SingleAllDay and AllDayRange are handled separately
                }
            }
        }
    }
}

private fun getDayOfWeekString(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "月"
        DayOfWeek.TUESDAY -> "火"
        DayOfWeek.WEDNESDAY -> "水"
        DayOfWeek.THURSDAY -> "木"
        DayOfWeek.FRIDAY -> "金"
        DayOfWeek.SATURDAY -> "土"
        DayOfWeek.SUNDAY -> "日"
    }
}
