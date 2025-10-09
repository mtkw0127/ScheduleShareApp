package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back
import scheduleshare.composeapp.generated.resources.layers
import scheduleshare.composeapp.generated.resources.plus
import scheduleshare.composeapp.generated.resources.view_column
import kotlin.math.absoluteValue

@Composable
fun DayScheduleScreen(
    date: LocalDate,
    scheduleRepository: ScheduleRepository,
    userRepository: UserRepository,
    onBackClick: () -> Unit,
    onDateChange: (LocalDate) -> Unit = {},
    onAddScheduleClick: () -> Unit = {},
    onAddScheduleAtTime: (LocalTime) -> Unit = {},
    onScheduleClick: (Schedule) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var currentDate by remember { mutableStateOf(date) }
    var dragOffset by remember { mutableStateOf(0f) }
    var isColumnView by remember { mutableStateOf(false) } // 縦並び表示かどうか（一度trueになったら保持）

    val schedules = remember(currentDate) {
        scheduleRepository.getSchedulesByDate(currentDate)
    }

    // 表示対象のユーザー一覧を取得
    val visibleUsers = remember {
        val testUser = User.createTest()
        val sharedUsers = userRepository.getSharedUsers().filter { user ->
            userRepository.getUserVisibility(user.id)
        }
        listOf(testUser) + sharedUsers
    }

    // ユーザーごとにグループ化（visibilityがtrueのユーザーのみ）
    val schedulesByUser = remember(schedules, visibleUsers) {
        // 列表示の場合は予定がなくてもユーザーを表示するため、全ユーザーをマップに含める
        visibleUsers.associateWith { user ->
            schedules.filter { it.user.id == user.id }
        }
    }

    // ユーザーの色を取得する関数
    val getUserColor: (User.Id) -> Color = { userId ->
        Color(userRepository.getUserColor(userId).value)
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
                },
                actions = {
                    // 複数ユーザーの予定がある場合のみ切り替えボタンを表示
                    if (schedulesByUser.size > 1) {
                        IconButton(
                            onClick = { isColumnView = !isColumnView }
                        ) {
                            Icon(
                                imageVector = vectorResource(
                                    if (isColumnView) Res.drawable.layers else Res.drawable.view_column
                                ),
                                contentDescription = if (isColumnView) "重ねて表示" else "ユーザーごとに表示",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
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
            if (isColumnView && schedulesByUser.size > 1) {
                // ユーザーごとに横並び表示（列分割）
                // 終日予定の最大数を計算
                val maxAllDayCount = schedulesByUser.values.maxOfOrNull { userSchedules ->
                    userSchedules.count { it.isAllDay }
                } ?: 0

                Row(modifier = Modifier.fillMaxWidth()) {
                    // 左側固定: 時刻表示エリア
                    Column(
                        modifier = Modifier.width(60.dp)
                    ) {
                        // ユーザー名エリアの高さを合わせる (padding vertical 4.dp * 2 + titleMedium text height ~24.dp)
                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider()

                        // 終日エリアの高さを合わせる
                        if (maxAllDayCount > 0) {
                            // padding 8.dp * 2 + "終日" text 12.sp (~16.dp) + spacer 4.dp + (maxAllDayCount * (64.dp card + 4.dp spacer))
                            val allDayAreaHeight = 16.dp + 16.dp + 4.dp + (68.dp * maxAllDayCount)
                            Spacer(modifier = Modifier.height(allDayAreaHeight))
                        }

                        // 時刻ラベル
                        TimeLabelsColumn()
                    }

                    // 右側スクロール: ユーザーの列
                    val horizontalScrollState = rememberScrollState()
                    var horizontalDragOffset by remember { mutableStateOf(0f) }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        // スクロールが端に到達している場合のみ日付移動
                                        val threshold = 100f
                                        val isAtStart = horizontalScrollState.value == 0
                                        val isAtEnd =
                                            horizontalScrollState.value == horizontalScrollState.maxValue

                                        if (horizontalDragOffset.absoluteValue > threshold) {
                                            if (horizontalDragOffset > 0 && isAtStart) {
                                                // 右スワイプ & 左端: 前日へ
                                                val newDate =
                                                    currentDate.plus(DatePeriod(days = -1))
                                                currentDate = newDate
                                                onDateChange(newDate)
                                            } else if (horizontalDragOffset < 0 && isAtEnd) {
                                                // 左スワイプ & 右端: 翌日へ
                                                val newDate = currentDate.plus(DatePeriod(days = 1))
                                                currentDate = newDate
                                                onDateChange(newDate)
                                            }
                                        }
                                        horizontalDragOffset = 0f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        horizontalDragOffset += dragAmount
                                        scope.launch {
                                            horizontalScrollState.scrollBy(-dragAmount)
                                        }
                                    }
                                )
                            }
                    ) {
                        schedulesByUser.forEach { (user, userSchedules) ->
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 2.dp)
                            ) {
                                // ユーザー名表示
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                HorizontalDivider()

                                // 終日の予定エリア（高さを統一）
                                val allDaySchedules = userSchedules.filter { it.isAllDay }
                                if (maxAllDayCount > 0) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (allDaySchedules.isNotEmpty())
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else
                                                    MaterialTheme.colorScheme.surface
                                            )
                                            .padding(8.dp)
                                    ) {
                                        // 全ユーザーで"終日"テキストを表示（高さを統一するため）
                                        Text(
                                            text = "終日",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (allDaySchedules.isNotEmpty())
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            else
                                                MaterialTheme.colorScheme.surface // 透明にする
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // 実際の終日予定を表示（固定の高さ）
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
                                            Spacer(modifier = Modifier.height(68.dp)) // カード1つ分の高さ(64dp) + spacer(4dp)
                                        }
                                    }
                                }

                                // 時間軸と予定を表示（時刻ラベルなし）
                                val timedSchedules = userSchedules.filter { it.isTimed }
                                TimelineView(
                                    timedSchedules = timedSchedules,
                                    onScheduleClick = onScheduleClick,
                                    onTimelineClick = onAddScheduleAtTime,
                                    showTimeLabels = false,
                                    getUserColor = getUserColor
                                )
                            }
                        }
                    }
                }
            } else {
                // 従来通りの重ねて表示
                // 終日の予定を最初に表示
                val allDaySchedules = schedules.filter { it.isAllDay }
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        allDaySchedules.forEach { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                containerColor = getUserColor(schedule.user.id),
                                onClick = { onScheduleClick(schedule) }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // 時間軸と予定を表示
                val timedSchedules = schedules.filter { it.isTimed }
                TimelineView(
                    timedSchedules = timedSchedules,
                    onScheduleClick = onScheduleClick,
                    onTimelineClick = onAddScheduleAtTime,
                    getUserColor = getUserColor
                )
            }
        }
    }
}

@Composable
private fun TimeLabelsColumn() {
    val hourHeight = 60.dp
    Column {
        (0..23).forEach { hour ->
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(hourHeight)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "${hour.toString().padStart(2, '0')}:00",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimelineView(
    timedSchedules: List<Schedule>,
    onScheduleClick: (Schedule) -> Unit = {},
    onTimelineClick: (LocalTime) -> Unit = {},
    showTimeLabels: Boolean = true,
    getUserColor: (User.Id) -> Color = { Color.Unspecified }
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
                    if (showTimeLabels) {
                        // 時刻表示部分（1人目のみ）
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
                    }

                    // スケジュール配置用のスペース（クリック可能）
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onTimelineClick(LocalTime(hour, 0))
                            }
                    )
                }
            }
        }

        // 予定を絶対配置
        Layout(
            content = {
                timedSchedules.forEach { schedule ->
                    if (schedule.isTimed) {
                        ScheduleCard(
                            schedule = schedule,
                            containerColor = getUserColor(schedule.user.id),
                            onClick = { onScheduleClick(schedule) }
                        )
                    }
                }
            },
            modifier = Modifier.padding(start = if (showTimeLabels) 60.dp else 0.dp)
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
                val startMinutes =
                    checkNotNull(schedule.startDateTime.time).hour * 60 + checkNotNull(schedule.startDateTime.time).minute
                val endMinutes =
                    checkNotNull(schedule.endDateTime.time).hour * 60 + checkNotNull(schedule.endDateTime.time).minute

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
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(4.dp)
        ) {
            val availableHeight = maxHeight

            // 高さに応じて表示内容を調整
            val displayTitle = if (schedule.isMultiDay) {
                "${schedule.title} (${schedule.startDateTime.date.dayOfMonth}日〜${schedule.endDateTime.date.dayOfMonth}日)"
            } else {
                schedule.title
            }

            when {
                // 60dp未満: タイトルのみ、1行に省略
                availableHeight < 60.dp -> {
                    Text(
                        text = displayTitle,
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
                            text = displayTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (schedule.isTimed) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${schedule.startDateTime.time} - ${schedule.endDateTime.time}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
                // 100dp以上: 全て表示
                else -> {
                    Column {
                        Text(
                            text = displayTitle,
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
                        if (schedule.isTimed) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${schedule.startDateTime.time} - ${schedule.endDateTime.time}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
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
