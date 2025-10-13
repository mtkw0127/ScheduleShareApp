package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import com.github.mtkw0127.scheduleshare.components.TimeLabelsColumn
import com.github.mtkw0127.scheduleshare.extension.toJapanese
import com.github.mtkw0127.scheduleshare.extension.toYmd
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.model.user.UserColor
import com.github.mtkw0127.scheduleshare.repository.HolidayRepository
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.repository.UserRepository
import com.github.mtkw0127.scheduleshare.shared.preferences.SharedUserPreferenceRepository
import com.github.mtkw0127.scheduleshare.shared.preferences.UserPreferenceRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back
import scheduleshare.composeapp.generated.resources.layers
import scheduleshare.composeapp.generated.resources.plus
import scheduleshare.composeapp.generated.resources.view_column
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun DayScheduleScreen(
    date: LocalDate,
    scheduleRepository: ScheduleRepository,
    userRepository: UserRepository,
    userPreferenceRepository: UserPreferenceRepository,
    sharedUserPreferenceRepository: SharedUserPreferenceRepository,
    holidayRepository: HolidayRepository = HolidayRepository(),
    onBackClick: () -> Unit,
    onDateChange: (LocalDate) -> Unit = {},
    onAddScheduleClick: () -> Unit = {},
    onAddScheduleAtTime: (LocalTime) -> Unit = {},
    onScheduleClick: (Schedule) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var currentDate by remember { mutableStateOf(date) }
    var dragOffset by remember { mutableStateOf(0f) }

    // DataStoreから表示モードを取得
    var isColumnView by remember { mutableStateOf(true) }

    // 初回読み込み時にDataStoreから設定を取得
    LaunchedEffect(Unit) {
        isColumnView = userPreferenceRepository.isColumnLayoutEnabled()
    }

    // 表示対象のユーザー一覧を取得（DataStoreから読み込み）
    var visibleUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var userColorMap by remember { mutableStateOf<Map<User.Id, UserColor>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val testUser = User.createTest()
        val allSharedUsers = userRepository.getSharedUsers()
        val filteredUsers = mutableListOf<User>()
        val colorMap = mutableMapOf<User.Id, UserColor>()

        // testユーザーの色を設定
        colorMap[testUser.id] = UserColor.default()

        // 各ユーザーのvisibilityとcolorをDataStoreから取得
        for (user in allSharedUsers) {
            val isVisible = sharedUserPreferenceRepository.getUserVisibility(user.id.value)
            if (isVisible) {
                filteredUsers.add(user)
            }

            // 色を取得
            val savedColor = sharedUserPreferenceRepository.getUserColor(user.id.value)
            val color = if (savedColor != null) {
                UserColor.fromValue(savedColor)
            } else {
                UserColor.default()
            }
            colorMap[user.id] = color
        }

        visibleUsers = listOf(testUser) + filteredUsers
        userColorMap = colorMap
    }

    val schedules = remember(currentDate, visibleUsers) {
        val allSchedules = scheduleRepository.getSchedulesByDate(currentDate)
        // visibleUsersに含まれるユーザーの予定のみをフィルタリング
        val visibleUserIds = visibleUsers.map { it.id }.toSet()
        allSchedules.filter { schedule ->
            visibleUserIds.contains(schedule.createUser.id)
        }
    }

    // 祝日を取得
    val holiday = remember(currentDate) {
        val holidays = holidayRepository.getJapaneseHolidays(currentDate.year)
        holidays.find { it.date == currentDate }
    }

    // ユーザーごとにグループ化（visibilityがtrueのユーザーのみ）
    val schedulesByUser = remember(schedules, visibleUsers) {
        // 列表示の場合は予定がなくてもユーザーを表示するため、全ユーザーをマップに含める
        visibleUsers.associateWith { user ->
            schedules.filter { it.createUser.id == user.id }
        }
    }

    // ユーザーの色を取得する関数
    val getUserColor: (User.Id) -> Color = { userId ->
        Color((userColorMap[userId] ?: UserColor.default()).value)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
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
                            onClick = {
                                val newValue = !isColumnView
                                isColumnView = newValue
                                // DataStoreに保存
                                scope.launch {
                                    userPreferenceRepository.setColumnLayoutEnabled(newValue)
                                }
                            }
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
        val density = LocalDensity.current
        var allDayHeight by remember { mutableStateOf(0.dp) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isColumnView && schedulesByUser.size > 1) {
                // 横スクロール状態を共有
                val sharedHorizontalScrollState = rememberScrollState()

                // 2人の場合は均等分割、3人以上の場合は固定幅
                val userCount = schedulesByUser.size
                val useTwoColumnLayout = userCount == 2

                Column(modifier = Modifier.fillMaxSize()) {
                    // ユーザー名ヘッダー（固定）
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // 左側固定: 時刻表示エリアの幅を合わせる
                        Spacer(modifier = Modifier.width(60.dp))

                        // 右側スクロール: ユーザー名
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (useTwoColumnLayout) Modifier
                                    else Modifier.horizontalScroll(sharedHorizontalScrollState)
                                )
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            schedulesByUser.keys.forEach { user ->
                                Column(
                                    modifier = if (useTwoColumnLayout) {
                                        Modifier
                                            .weight(1f)
                                    } else {
                                        Modifier
                                            .width(150.dp)
                                    }.padding(2.dp)
                                ) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
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
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        val threshold = 100f
                                        if (dragOffset.absoluteValue > threshold) {
                                            val newDate = if (dragOffset > 0) {
                                                currentDate.plus(DatePeriod(days = -1))
                                            } else {
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
                    ) {
                        // ユーザーごとに横並び表示（列分割）
                        // 終日予定の最大数を計算（祝日も含む、最低1つは表示）
                        val maxAllDayCount = (schedulesByUser.values.maxOfOrNull { userSchedules ->
                            userSchedules.count { it.isAllDay }
                        } ?: 0).let { max ->
                            if (holiday != null) (max + 1).coerceAtLeast(1) else max.coerceAtLeast(1)
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // 左側固定: 時刻表示エリア
                            Column(
                                modifier = Modifier.width(60.dp)
                            ) {
                                // 終日エリアの高さを合わせる（常に表示）
                                Spacer(modifier = Modifier.height(allDayHeight))

                                // 時刻ラベル
                                TimeLabelsColumn()
                            }

                            // 右側スクロール: ユーザーの列
                            var overScrollOffset by remember { mutableStateOf(0f) }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (userCount <= 2) {
                                            Modifier.pointerInput(
                                                useTwoColumnLayout,
                                                sharedHorizontalScrollState
                                            ) {
                                                var horizontalDragOffset = 0f
                                                detectHorizontalDragGestures(
                                                    onDragEnd = {
                                                        val threshold = 100f
                                                        // 2人の場合は常に日付移動
                                                        val canNavigate = useTwoColumnLayout

                                                        if (horizontalDragOffset.absoluteValue > threshold && canNavigate) {
                                                            val newDate =
                                                                if (horizontalDragOffset > 0) {
                                                                    currentDate.plus(DatePeriod(days = -1))
                                                                } else {
                                                                    currentDate.plus(DatePeriod(days = 1))
                                                                }
                                                            currentDate = newDate
                                                            onDateChange(newDate)
                                                        }
                                                        horizontalDragOffset = 0f
                                                    },
                                                    onHorizontalDrag = { _, dragAmount ->
                                                        horizontalDragOffset += dragAmount
                                                    }
                                                )
                                            }
                                        } else {
                                            Modifier.horizontalScroll(sharedHorizontalScrollState)
                                        }
                                    )
                                    .then(
                                        if (userCount > 2) {
                                            Modifier.pointerInput(sharedHorizontalScrollState) {
                                                awaitEachGesture {
                                                    awaitFirstDown(requireUnconsumed = false)

                                                    do {
                                                        val event = awaitPointerEvent()
                                                        event.changes.firstOrNull()?.let { change ->
                                                            val dragAmount = change.positionChange().x

                                                            // スクロールが端にある場合のみover-scrollを検出
                                                            val isAtStart = sharedHorizontalScrollState.value == 0
                                                            val isAtEnd = sharedHorizontalScrollState.value >= sharedHorizontalScrollState.maxValue

                                                            if ((isAtStart && dragAmount > 0) || (isAtEnd && dragAmount < 0)) {
                                                                overScrollOffset += dragAmount
                                                            } else {
                                                                overScrollOffset = 0f
                                                            }
                                                        }
                                                    } while (event.changes.any { it.pressed })

                                                    // ドラッグ終了時の処理
                                                    val threshold = 100f
                                                    if (overScrollOffset.absoluteValue > threshold) {
                                                        val newDate = if (overScrollOffset > 0) {
                                                            currentDate.plus(DatePeriod(days = -1))
                                                        } else {
                                                            currentDate.plus(DatePeriod(days = 1))
                                                        }
                                                        currentDate = newDate
                                                        onDateChange(newDate)
                                                    }
                                                    overScrollOffset = 0f
                                                }
                                            }
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                var index = 0
                                schedulesByUser.forEach { (user, userSchedules) ->
                                    val isFirstUser = index == 0
                                    index++
                                    Column(
                                        modifier = if (useTwoColumnLayout) {
                                            Modifier
                                                .weight(1f)
                                        } else {
                                            Modifier
                                                .width(150.dp)
                                        }
                                            .fillMaxHeight()
                                            .padding(horizontal = 2.dp)
                                    ) {
                                        // 終日の予定エリア（高さを統一、常に表示）
                                        val allDaySchedules = userSchedules.filter { it.isAllDay }
                                        Column(
                                            modifier = Modifier
                                                .onSizeChanged {
                                                    allDayHeight = with(density) {
                                                        it.height.toDp()
                                                    }
                                                }
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(8.dp)

                                        ) {
                                            // 全ユーザーで"終日"テキストを表示（高さを統一するため）
                                            Text(
                                                text = "終日",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))

                                            // 最初のユーザーの列に祝日を表示
                                            if (isFirstUser && holiday != null) {
                                                Box(modifier = Modifier.height(64.dp)) {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = Color(0xFF4CAF50)
                                                        ),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(8.dp)
                                                        ) {
                                                            Text(
                                                                text = holiday.name,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }

                                            // 実際の終日予定を表示（固定の高さ）
                                            allDaySchedules.forEach { schedule ->
                                                Box(modifier = Modifier.height(64.dp)) {
                                                    ScheduleCard(
                                                        schedule = schedule,
                                                        containerColor = getUserColor(schedule.createUser.id),
                                                        onClick = { onScheduleClick(schedule) }
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }

                                            // 不足分は空のスペースで埋める
                                            val holidayCount =
                                                if (isFirstUser && holiday != null) 1 else 0
                                            val emptyCount =
                                                maxAllDayCount - allDaySchedules.size - holidayCount
                                            repeat(emptyCount) {
                                                Spacer(modifier = Modifier.height(68.dp)) // カード1つ分の高さ(64dp) + spacer(4dp)
                                            }
                                        }

                                        // 時間軸と予定を表示（時刻ラベルなし）
                                        val timedSchedules = userSchedules.filter { it.isTimed }
                                        TimelineView(
                                            timedSchedules = timedSchedules,
                                            currentDate = currentDate,
                                            onScheduleClick = onScheduleClick,
                                            onTimelineClick = onAddScheduleAtTime,
                                            showTimeLabels = false,
                                            getUserColor = getUserColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // 従来通りの重ねて表示
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    val threshold = 100f
                                    if (dragOffset.absoluteValue > threshold) {
                                        val newDate = if (dragOffset > 0) {
                                            currentDate.plus(DatePeriod(days = -1))
                                        } else {
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
                ) {
                    // 終日の予定と祝日を最初に表示
                    val allDaySchedules = schedules.filter { it.isAllDay }
                    if (allDaySchedules.isNotEmpty() || holiday != null) {
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

                            // 祝日を表示
                            if (holiday != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = holiday.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            allDaySchedules.forEach { schedule ->
                                ScheduleCard(
                                    schedule = schedule,
                                    containerColor = getUserColor(schedule.createUser.id),
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
                        currentDate = currentDate,
                        onScheduleClick = onScheduleClick,
                        onTimelineClick = onAddScheduleAtTime,
                        getUserColor = getUserColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun TimelineView(
    timedSchedules: List<Schedule>,
    onScheduleClick: (Schedule) -> Unit = {},
    onTimelineClick: (LocalTime) -> Unit = {},
    showTimeLabels: Boolean = true,
    getUserColor: (User.Id) -> Color = { Color.Unspecified },
    currentDate: LocalDate
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

        // 現在時刻の線
        if (now != null) {
            val currentMinutes = now.hour * 60 + now.minute
            val offsetY = (currentMinutes / 60f) * hourHeight.value
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .padding(start = if (showTimeLabels) 60.dp else 0.dp)
                    .offset(y = offsetY.dp)
                    .background(Color.Red)
            )
        }

        // 予定を絶対配置
        Layout(
            content = {
                timedSchedules.forEach { schedule ->
                    if (schedule.isTimed) {
                        ScheduleCard(
                            schedule = schedule,
                            containerColor = getUserColor(schedule.createUser.id),
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
fun ScheduleCard(
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
                        color = Color.White,
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
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (schedule.isTimed) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${schedule.startDateTime.time} - ${schedule.endDateTime.time}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f),
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
                            color = Color.White
                        )
                        if (schedule.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.description,
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (schedule.isTimed) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${schedule.startDateTime.time} - ${schedule.endDateTime.time}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}