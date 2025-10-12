package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import com.github.mtkw0127.scheduleshare.extension.toYm
import com.github.mtkw0127.scheduleshare.model.calendar.Day
import com.github.mtkw0127.scheduleshare.model.calendar.Month
import com.github.mtkw0127.scheduleshare.model.calendar.Week
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.model.user.UserColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_drop_down
import scheduleshare.composeapp.generated.resources.menu
import scheduleshare.composeapp.generated.resources.qr_code
import scheduleshare.composeapp.generated.resources.user
import scheduleshare.composeapp.generated.resources.view_week
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun CalendarScreen(
    months: List<Month>,
    focusedMonth: LocalDate,
    schedules: Map<LocalDate, List<Schedule>>,
    sharedUsers: List<User>,
    userVisibilityMap: Map<User.Id, Boolean>,
    userColorMap: Map<User.Id, UserColor>,
    moveToPrev: () -> Unit,
    moveToNext: () -> Unit,
    onClickDate: (Day) -> Unit = {},
    onUserIconClick: () -> Unit = {},
    onQRShareClick: () -> Unit = {},
    onWeekScheduleClick: () -> Unit = {},
    onDayScheduleClick: () -> Unit = {},
    onUserVisibilityChange: (User.Id, Boolean) -> Unit = { _, _ -> },
    onUserColorChange: (User.Id, UserColor) -> Unit = { _, _ -> }
) {
    val state = rememberLazyListState()
    var changingFocus by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var viewModeMenuExpanded by remember { mutableStateOf(false) }
    var selectedViewMode by remember { mutableStateOf("月") }

    // 各ユーザーの表示状態を管理
    val userVisibilityState = remember(sharedUsers, userVisibilityMap) {
        mutableStateOf(sharedUsers.associateWith { user ->
            userVisibilityMap[user.id] ?: true
        }.toMutableMap())
    }

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "共有ユーザー",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    sharedUsers.forEach { user ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newChecked = !(userVisibilityState.value[user] ?: true)
                                        userVisibilityState.value =
                                            userVisibilityState.value.toMutableMap().apply {
                                                this[user] = newChecked
                                            }
                                        onUserVisibilityChange(user.id, newChecked)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = userVisibilityState.value[user] ?: true,
                                    onCheckedChange = { checked ->
                                        userVisibilityState.value =
                                            userVisibilityState.value.toMutableMap().apply {
                                                this[user] = checked
                                            }
                                        onUserVisibilityChange(user.id, checked)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // 色選択
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 48.dp, top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val currentColor = userColorMap[user.id] ?: UserColor.default()
                                UserColor.entries.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = Color(color.value),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = if (currentColor == color) 3.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                onUserColorChange(user.id, color)
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // QRコード共有ボタン
                    Button(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onQRShareClick()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.qr_code),
                            contentDescription = "QRコード",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("QRコードで共有")
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    title = {
                        Text(
                            text = focusedMonth.toYm(),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.menu),
                                contentDescription = "メニュー",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        // 表示モード切り替えドロップダウン
                        Box {
                            IconButton(onClick = { viewModeMenuExpanded = true }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = selectedViewMode,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.arrow_drop_down),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = viewModeMenuExpanded,
                                onDismissRequest = { viewModeMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("日")
                                            if (selectedViewMode == "日") {
                                                Text("✓", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedViewMode = "日"
                                        viewModeMenuExpanded = false
                                        onDayScheduleClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("週")
                                            if (selectedViewMode == "週") {
                                                Text("✓", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedViewMode = "週"
                                        viewModeMenuExpanded = false
                                        onWeekScheduleClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("月")
                                            if (selectedViewMode == "月") {
                                                Text("✓", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedViewMode = "月"
                                        viewModeMenuExpanded = false
                                    }
                                )
                            }
                        }

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
                            DateView(
                                month,
                                schedules,
                                userColorMap,
                                onClickDate,
                                screenWidth,
                                screenHeight
                            )
                        }
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
    schedules: Map<LocalDate, List<Schedule>>,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp
) {
    Column(
        modifier = Modifier
            .width(screenWidth)
            .height(screenHeight)
    ) {
        Week(month.firstWeek, schedules, userColorMap, onClickDate, Modifier.weight(1F))
        Week(month.secondWeek, schedules, userColorMap, onClickDate, Modifier.weight(1F))
        Week(month.thirdWeek, schedules, userColorMap, onClickDate, Modifier.weight(1F))
        Week(month.fourthWeek, schedules, userColorMap, onClickDate, Modifier.weight(1F))
        Week(month.fifthWeek, schedules, userColorMap, onClickDate, Modifier.weight(1F))
        month.sixthWeek?.let { week ->
            Week(week, schedules, userColorMap, onClickDate, Modifier.weight(1F))
        }
    }
}

@Composable
private fun Week(
    week: Week,
    schedules: Map<LocalDate, List<Schedule>>,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit,
    modifier: Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        DateCell(
            day = week.sunday,
            schedules = schedules,
            userColorMap = userColorMap,
            onClickDate = onClickDate,
            modifier = Modifier.weight(1F)
        )
        DateCell(
            day = week.monday,
            schedules = schedules,
            userColorMap = userColorMap,
            onClickDate = onClickDate,
            modifier = Modifier.weight(1F)
        )
        DateCell(
            day = week.tuesday,
            schedules = schedules,
            userColorMap = userColorMap,
            onClickDate = onClickDate,
            modifier = Modifier.weight(1F)
        )
        DateCell(
            day = week.wednesday,
            schedules = schedules,
            userColorMap = userColorMap,
            onClickDate = onClickDate,
            modifier = Modifier.weight(1F)
        )
        DateCell(
            day = week.thursday,
            schedules = schedules,
            userColorMap = userColorMap,
            onClickDate = onClickDate,
            modifier = Modifier.weight(1F)
        )
        DateCell(
            day = week.friday,
            schedules = schedules,
            userColorMap = userColorMap,
            onClickDate = onClickDate,
            modifier = Modifier.weight(1F)
        )
        DateCell(
            day = week.saturday,
            schedules = schedules,
            userColorMap = userColorMap,
            onClickDate = onClickDate,
            modifier = Modifier.weight(1F)
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DateCell(
    day: Day,
    schedules: Map<LocalDate, List<Schedule>>,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit,
    modifier: Modifier = Modifier,
) {
    val daySchedules = schedules[day.value]
        ?.filter {
            it.isTimed || it.isSingleAllDay
        } ?: emptyList()

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
            val userColor = userColorMap[schedule.user.id] ?: UserColor.default()
            val displayTitle = if (schedule.isMultiDay) {
                "${schedule.title} (${schedule.startDateTime.date.dayOfMonth}日〜${schedule.endDateTime.date.dayOfMonth}日)"
            } else {
                schedule.title
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 1.dp)
                    .background(
                        color = Color(userColor.value),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = displayTitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
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

