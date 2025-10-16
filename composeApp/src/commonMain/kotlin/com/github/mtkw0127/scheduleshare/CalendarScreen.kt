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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
import com.github.mtkw0127.scheduleshare.model.schedule.ScheduleTime
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.model.user.UserColor
import com.github.mtkw0127.scheduleshare.repository.HolidayRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_drop_down
import scheduleshare.composeapp.generated.resources.menu
import scheduleshare.composeapp.generated.resources.qr_code
import scheduleshare.composeapp.generated.resources.user
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun CalendarScreen(
    months: List<Month>,
    focusedMonth: LocalDate,
    schedules: Map<LocalDate, List<Schedule>>,
    holidays: Map<LocalDate, HolidayRepository.Holiday>,
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
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
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

                        IconButton(onClick = onQRShareClick) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.qr_code),
                                contentDescription = "QRコード",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
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
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .pointerInput(Unit) {
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
                            DayView(
                                screenWidth = screenWidth,
                                focusedMonth = month.firstDay
                            )
                            DateView(
                                month,
                                schedules,
                                holidays,
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

@OptIn(ExperimentalTime::class)
@Composable
private fun DayView(
    screenWidth: Dp,
    focusedMonth: LocalDate
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val todayDayOfWeek = today.dayOfWeek

    // 表示されている月が今月かどうかをチェック
    val isCurrentMonth = focusedMonth.year == today.year && focusedMonth.month == today.month

    Row(
        modifier = Modifier
            .width(screenWidth)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DayCell(
            day = "日",
            modifier = Modifier.weight(1F),
            textColor = Color.Red,
            isBold = isCurrentMonth && todayDayOfWeek == kotlinx.datetime.DayOfWeek.SUNDAY
        )
        DayCell(
            day = "月",
            modifier = Modifier.weight(1F),
            isBold = isCurrentMonth && todayDayOfWeek == kotlinx.datetime.DayOfWeek.MONDAY
        )
        DayCell(
            day = "火",
            modifier = Modifier.weight(1F),
            isBold = isCurrentMonth && todayDayOfWeek == kotlinx.datetime.DayOfWeek.TUESDAY
        )
        DayCell(
            day = "水",
            modifier = Modifier.weight(1F),
            isBold = isCurrentMonth && todayDayOfWeek == kotlinx.datetime.DayOfWeek.WEDNESDAY
        )
        DayCell(
            day = "木",
            modifier = Modifier.weight(1F),
            isBold = isCurrentMonth && todayDayOfWeek == kotlinx.datetime.DayOfWeek.THURSDAY
        )
        DayCell(
            day = "金",
            modifier = Modifier.weight(1F),
            isBold = isCurrentMonth && todayDayOfWeek == kotlinx.datetime.DayOfWeek.FRIDAY
        )
        DayCell(
            day = "土",
            modifier = Modifier.weight(1F),
            textColor = Color.Blue,
            isBold = isCurrentMonth && todayDayOfWeek == kotlinx.datetime.DayOfWeek.SATURDAY
        )
    }
}

@Composable
private fun DayCell(
    day: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
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
                    strokeWidth = 0.5f
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DateView(
    month: Month,
    schedules: Map<LocalDate, List<Schedule>>,
    holidays: Map<LocalDate, HolidayRepository.Holiday>,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val currentYear = month.firstDay.year
    val currentMonth = month.firstDay.month

    Column(
        modifier = Modifier
            .width(screenWidth)
            .height(screenHeight)
    ) {
        Week(
            month.firstWeek,
            schedules,
            holidays,
            userColorMap,
            onClickDate,
            currentYear,
            currentMonth,
            Modifier.weight(1F)
        )
        Week(
            month.secondWeek,
            schedules,
            holidays,
            userColorMap,
            onClickDate,
            currentYear,
            currentMonth,
            Modifier.weight(1F)
        )
        Week(
            month.thirdWeek,
            schedules,
            holidays,
            userColorMap,
            onClickDate,
            currentYear,
            currentMonth,
            Modifier.weight(1F)
        )
        Week(
            month.fourthWeek,
            schedules,
            holidays,
            userColorMap,
            onClickDate,
            currentYear,
            currentMonth,
            Modifier.weight(1F)
        )
        Week(
            month.fifthWeek,
            schedules,
            holidays,
            userColorMap,
            onClickDate,
            currentYear,
            currentMonth,
            Modifier.weight(1F)
        )
        month.sixthWeek?.let { week ->
            Week(
                week,
                schedules,
                holidays,
                userColorMap,
                onClickDate,
                currentYear,
                currentMonth,
                Modifier.weight(1F)
            )
        }
    }
}

@Composable
private fun Week(
    week: Week,
    schedules: Map<LocalDate, List<Schedule>>,
    holidays: Map<LocalDate, HolidayRepository.Holiday>,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit,
    currentYear: Int,
    currentMonth: kotlinx.datetime.Month,
    modifier: Modifier,
) {
    val firstDateOfWeek = week.sunday.value
    val weekDays = listOf(
        week.sunday,
        week.monday,
        week.tuesday,
        week.wednesday,
        week.thursday,
        week.friday,
        week.saturday
    )

    // 先頭が日曜日で末尾が土曜
    // 各曜日に配置した予定の行数を保存する
    var blockNum by remember { mutableStateOf(listOf(0, 0, 0, 0, 0, 0, 0)) }

    // 週内の連日予定を抽出
    val thisWeekMultiSchedules = schedules.values
        .flatten()
        .filter {
            when (val time = it.time) {
                is ScheduleTime.SingleDateSchedule -> {
                    false
                }

                is ScheduleTime.MultiDateSchedule -> {
                    time.isThisWeek(firstDateOfWeek)
                }
            }
        }

    // 週内の単日予定を抽出（各日付ごとに）
    val thisWeekSingleSchedules = weekDays.mapIndexed { dayIndex, day ->
        dayIndex to (schedules[day.value]?.filter { schedule ->
            schedule.time is ScheduleTime.SingleDateSchedule
        } ?: emptyList())
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val dayCellWidth = maxWidth / 7
        var dayCellHeight by remember { mutableStateOf(0.dp) }
        var dayCellNumHeightDp by remember { mutableStateOf(0.dp) }
        var scheduleBarHeight by remember { mutableStateOf(0.dp) }
        val dayCellContentHeight by derivedStateOf {
            dayCellHeight - dayCellNumHeightDp
        }

        // 表示可能な最大スケジュール数を計算
        val maxVisibleSchedules by derivedStateOf {
            if (scheduleBarHeight > 0.dp && dayCellContentHeight > 0.dp) {
                (dayCellContentHeight / scheduleBarHeight).toInt()
            } else {
                Int.MAX_VALUE
            }
        }

        // 日付セル
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                DateCell(
                    day = day,
                    onClickDate = onClickDate,
                    onUpdateDateCellNumHeight = { height ->
                        with(density) {
                            dayCellNumHeightDp = height.toDp()
                        }
                    },
                    onUpdateDateCellHeight = { height ->
                        with(density) {
                            dayCellHeight = height.toDp()
                        }
                    },
                    currentYear = currentYear,
                    currentMonth = currentMonth,
                    modifier = Modifier.weight(1F),
                )
            }
        }

        // 予定を横棒で表示
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // blockNumをリセット
            blockNum = listOf(0, 0, 0, 0, 0, 0, 0)

            // 1. まず祝日を表示（最優先）
            weekDays.forEachIndexed { dayIndex, day ->
                val holiday = holidays[day.value]
                if (holiday != null) {
                    val row = blockNum[dayIndex]

                    // 祝日バーを配置
                    Box(
                        modifier = Modifier
                            .offset(
                                x = dayCellWidth * dayIndex,
                                y = dayCellNumHeightDp + (scheduleBarHeight * row)
                            )
                            .width(dayCellWidth)
                            .height(scheduleBarHeight)
                            .padding(horizontal = 1.dp, vertical = 0.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.9f), // 緑色
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 3.5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🎌",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.absoluteOffset(0.dp, (-4).dp)
                            )
                            Text(
                                text = holiday.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.offset(y = (3).dp)
                            )
                        }
                    }

                    // この日のblockNumを更新
                    blockNum = blockNum.toMutableList().apply {
                        this[dayIndex] = row + 1
                    }
                }
            }

            // 2. 全ての予定をまとめてソート
            // 1. 翌週にまたがる予定を優先（開始日が週の最初より前、または終了日が週の最後より後）
            // 2. その次に開始日時順
            val lastDayOfWeek = weekDays.last().value
            val allSchedules =
                (thisWeekMultiSchedules + thisWeekSingleSchedules.flatMap { it.second })
                    .sortedWith(
                        compareByDescending<Schedule> { schedule ->
                            // 翌週にまたがるかチェック
                            when (schedule.time) {
                                is ScheduleTime.MultiDateSchedule -> {
                                    schedule.endDateTime.date > lastDayOfWeek || schedule.startDateTime.date < firstDateOfWeek
                                }

                                else -> false
                            }
                        }.thenBy { it.startDateTime }
                    )

            allSchedules.forEach { schedule ->
                val startDate = schedule.startDateTime.date

                when (val time = schedule.time) {
                    is ScheduleTime.MultiDateSchedule -> {
                        // この予定の開始位置（週の何日目か）
                        val startDayIndex = if (startDate <= firstDateOfWeek) {
                            0
                        } else {
                            firstDateOfWeek.daysUntil(startDate)
                        }

                        // この予定が占める期間の各曜日のblockNumの最大値を取得
                        val duration = time.duration()
                        val maxBlockInRange =
                            (startDayIndex until (startDayIndex + duration).coerceAtMost(7))
                                .maxOfOrNull { blockNum.getOrNull(it) ?: 0 } ?: 0

                        // この予定を配置する行
                        val row = maxBlockInRange

                        // 表示範囲内かチェック
                        if (row < maxVisibleSchedules) {
                            // この予定が占める範囲のblockNumを更新
                            blockNum = blockNum.toMutableList().apply {
                                for (i in startDayIndex until (startDayIndex + duration).coerceAtMost(
                                    7
                                )) {
                                    this[i] = row + 1
                                }
                            }

                            // 前週から繋がっているか
                            val continuesFromPrevWeek = startDate < firstDateOfWeek
                            // 翌週に繋がっているか
                            val continuesToNextWeek =
                                schedule.endDateTime.date > weekDays.last().value

                            ScheduleBar(
                                schedule = schedule,
                                userColorMap = userColorMap,
                                xOffset = dayCellWidth * startDayIndex,
                                yOffset = dayCellNumHeightDp * (row + 1),
                                width = dayCellWidth * duration,
                                continuesFromPrevWeek = continuesFromPrevWeek,
                                continuesToNextWeek = continuesToNextWeek,
                                onUpdateHeight = { height ->
                                    with(density) {
                                        scheduleBarHeight = height.toDp()
                                    }
                                }
                            )
                        }
                    }

                    is ScheduleTime.SingleDateSchedule -> {
                        val dayIndex = firstDateOfWeek.daysUntil(startDate)

                        if (dayIndex in 0..6) {
                            // この日のblockNumを取得
                            val row = blockNum[dayIndex]

                            // 表示範囲内かチェック
                            if (row < maxVisibleSchedules) {
                                // blockNumを更新
                                blockNum = blockNum.toMutableList().apply {
                                    this[dayIndex] = row + 1
                                }

                                ScheduleBar(
                                    schedule = schedule,
                                    userColorMap = userColorMap,
                                    xOffset = dayCellWidth * dayIndex,
                                    yOffset = dayCellNumHeightDp * (row + 1),
                                    width = dayCellWidth,
                                    onUpdateHeight = { height ->
                                        with(density) {
                                            scheduleBarHeight = height.toDp()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleBar(
    schedule: Schedule,
    userColorMap: Map<User.Id, UserColor>,
    xOffset: Dp,
    yOffset: Dp,
    width: Dp,
    continuesFromPrevWeek: Boolean = false,
    continuesToNextWeek: Boolean = false,
    onUpdateHeight: (Int) -> Unit = {}
) {
    val userColor = userColorMap[schedule.createUser.id] ?: UserColor.default()

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .onSizeChanged {
                    onUpdateHeight(it.height)
                }
                .offset(x = xOffset, y = yOffset)
                .width(width)
                .padding(horizontal = 1.dp, vertical = 0.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(userColor.value).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(
                            topStart = if (continuesFromPrevWeek) 0.dp else 10.dp,
                            bottomStart = if (continuesFromPrevWeek) 0.dp else 10.dp,
                            topEnd = if (continuesToNextWeek) 0.dp else 10.dp,
                            bottomEnd = if (continuesToNextWeek) 0.dp else 10.dp
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color(userColor.value),
                        shape = RoundedCornerShape(
                            topStart = if (continuesFromPrevWeek) 0.dp else 10.dp,
                            bottomStart = if (continuesFromPrevWeek) 0.dp else 10.dp,
                            topEnd = if (continuesToNextWeek) 0.dp else 10.dp,
                            bottomEnd = if (continuesToNextWeek) 0.dp else 10.dp
                        )
                    )
                    .padding(horizontal = 3.5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = schedule.title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DateCell(
    day: Day,
    onClickDate: (Day) -> Unit,
    onUpdateDateCellNumHeight: (Int) -> Unit,
    onUpdateDateCellHeight: (Int) -> Unit,
    currentYear: Int,
    currentMonth: kotlinx.datetime.Month,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .onSizeChanged {
                onUpdateDateCellHeight(it.height)
            }
            .fillMaxHeight()
            .fillMaxWidth()
            .border(0.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
            .clickable {
                onClickDate(day)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val color = MaterialTheme.colorScheme.secondary
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val isCurrentMonth = day.value.year == currentYear && day.value.month == currentMonth

        Box(
            modifier = Modifier
                .onSizeChanged {
                    onUpdateDateCellNumHeight(it.height)
                }
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
                text = day.value.day.toString(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = if (day.value == today) {
                    Color.White
                } else if (isCurrentMonth) {
                    when (day.value.dayOfWeek) {
                        kotlinx.datetime.DayOfWeek.SUNDAY -> Color.Red
                        kotlinx.datetime.DayOfWeek.SATURDAY -> Color.Blue
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                } else {
                    when (day.value.dayOfWeek) {
                        kotlinx.datetime.DayOfWeek.SUNDAY -> Color.Red.copy(alpha = 0.4f)
                        kotlinx.datetime.DayOfWeek.SATURDAY -> Color.Blue.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                }
            )
        }
    }
}

