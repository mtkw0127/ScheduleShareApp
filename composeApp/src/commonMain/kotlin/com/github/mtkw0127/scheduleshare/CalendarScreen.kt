package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import com.github.mtkw0127.scheduleshare.extension.toYm
import com.github.mtkw0127.scheduleshare.model.calendar.Day
import com.github.mtkw0127.scheduleshare.model.calendar.Month
import com.github.mtkw0127.scheduleshare.model.calendar.Week
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.user
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun CalendarScreen(
    months: List<Month>,
    focusedMonth: LocalDate,
    schedules: Map<LocalDate, List<com.github.mtkw0127.scheduleshare.model.schedule.Schedule>>,
    moveToPrev: () -> Unit,
    moveToNext: () -> Unit,
    onClickDate: (Day) -> Unit = {},
    onUserIconClick: () -> Unit = {},
) {
    val state = rememberLazyListState()
    var changingFocus by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = focusedMonth) {
        if (initialized) {
            val index = months.indexOfFirst { it.firstDay == focusedMonth }
            if (index >= 0) {
                state.animateScrollToItem(index)
            }
        }
    }

    LaunchedEffect(changingFocus) {
        if (changingFocus) {
            delay(200)
            changingFocus = false
        }
    }

    LaunchedEffect(months.isNotEmpty()) {
        if (initialized.not() && months.isNotEmpty()) {
            delay(200)
            state.animateScrollToItem(1)
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = focusedMonth.toYm(),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUserIconClick) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.user),
                            contentDescription = "ユーザー",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) {
        BoxWithConstraints(modifier = Modifier.padding(it)) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            LazyRow(
                state = state,
                userScrollEnabled = false,
            ) {
                items(months.size) { index ->
                    val month = months[index]
                    Column(
                        modifier = Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount.absoluteValue > 20 && changingFocus.not()) {
                                    changingFocus = true
                                    if (dragAmount > 0) {
                                        moveToPrev()
                                    } else {
                                        moveToNext()
                                    }
                                }
                            }
                        }
                    ) {
                        DayView(screenWidth)
                        DateView(month, schedules, onClickDate, screenWidth, screenHeight)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayView(screenWidth: Dp) {
    Row(
        modifier = Modifier
            .width(screenWidth)
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DayCell(
            day = "日",
            modifier = Modifier.weight(1F),
            textColor = Color.Red
        )
        DayCell(day = "月", modifier = Modifier.weight(1F))
        DayCell(day = "火", modifier = Modifier.weight(1F))
        DayCell(day = "水", modifier = Modifier.weight(1F))
        DayCell(day = "木", modifier = Modifier.weight(1F))
        DayCell(day = "金", modifier = Modifier.weight(1F))
        DayCell(
            day = "土",
            modifier = Modifier.weight(1F),
            textColor = Color.Blue
        )
    }
}

@Composable
private fun DayCell(
    day: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val lineColor = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = modifier
            .wrapContentHeight()
            .drawBehind {
                drawLine(
                    color = lineColor,
                    start = Offset(size.width, size.height),
                    end = Offset(size.width, size.height + 100),
                    strokeWidth = 2.0f
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day,
            fontSize = 12.sp,
            color = textColor
        )
    }
}

@Composable
private fun DateView(
    month: Month,
    schedules: Map<LocalDate, List<com.github.mtkw0127.scheduleshare.model.schedule.Schedule>>,
    onClickDate: (Day) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp
) {
    Column(
        modifier = Modifier
            .width(screenWidth)
            .height(screenHeight)
    ) {
        Week(month.firstWeek, schedules, onClickDate, Modifier.weight(1F))
        Week(month.secondWeek, schedules, onClickDate, Modifier.weight(1F))
        Week(month.thirdWeek, schedules, onClickDate, Modifier.weight(1F))
        Week(month.fourthWeek, schedules, onClickDate, Modifier.weight(1F))
        Week(month.fifthWeek, schedules, onClickDate, Modifier.weight(1F))
        month.sixthWeek?.let { week ->
            Week(week, schedules, onClickDate, Modifier.weight(1F))
        }
    }
}

@Composable
private fun Week(
    week: Week,
    schedules: Map<LocalDate, List<com.github.mtkw0127.scheduleshare.model.schedule.Schedule>>,
    onClickDate: (Day) -> Unit,
    modifier: Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        DateCell(
            day = week.sunday,
            schedules = schedules,
            onClickDate,
            Modifier.weight(1F)
        )
        DateCell(
            day = week.monday,
            schedules = schedules,
            onClickDate,
            Modifier.weight(1F)
        )
        DateCell(
            day = week.tuesday,
            schedules = schedules,
            onClickDate,
            Modifier.weight(1F)
        )
        DateCell(
            day = week.wednesday,
            schedules = schedules,
            onClickDate,
            Modifier.weight(1F)
        )
        DateCell(
            day = week.thursday,
            schedules = schedules,
            onClickDate,
            Modifier.weight(1F)
        )
        DateCell(
            day = week.friday,
            schedules = schedules,
            onClickDate,
            Modifier.weight(1F)
        )
        DateCell(
            day = week.saturday,
            schedules = schedules,
            onClickDate,
            Modifier.weight(1F)
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DateCell(
    day: Day,
    schedules: Map<LocalDate, List<com.github.mtkw0127.scheduleshare.model.schedule.Schedule>>,
    onClickDate: (Day) -> Unit,
    modifier: Modifier = Modifier,
) {
    val daySchedules = schedules[day.value] ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .border(0.2.dp, MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                onClickDate(day)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val color = MaterialTheme.colorScheme.secondary
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        Box(
            modifier = Modifier
                .padding(vertical = 5.dp)
                .size(20.dp)
                .drawBehind {
                    if (day.value == today) {
                        drawOval(color = color)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.value.dayOfMonth.toString(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = if (day.value == today) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // 予定の表示（最大3件まで）
        daySchedules.take(3).forEach { schedule ->
            Text(
                text = schedule.title,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )
        }

        // 3件以上ある場合は「...」を表示
        if (daySchedules.size > 3) {
            Text(
                text = "...",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

