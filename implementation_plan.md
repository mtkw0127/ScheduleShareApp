# 実装計画：月間リストビュー機能の追加

## 概要
CalendarScreen内の表示モード切り替えメニューに「リスト」オプションを追加し、同一画面内で月間カレンダービューと月間リストビューを切り替えられるようにします。リストビューは2列レイアウト（日付列・予定列）で1ヶ月分の予定を縦スクロールで表示し、共有ユーザーの予定を色分け・アイコン付きで混在表示します。

## ユーザー要件
- **表示期間**: 1ヶ月分
- **切り替え方法**: TopAppBarのドロップダウンメニュー
- **予定表示**: 混在表示、色分け表示、アイコン付き

## 設計方針
- **単一画面設計**: CalendarScreenが全ての表示モードを管理
- **TopAppBar共通化**: 既存のドロップダウンメニューを拡張
- **Composable切り替え**: 選択されたモードに応じて内部のComposableを切り替え
- **ナビゲーション不要**: 画面遷移なし（状態管理のみ）

## 実装内容

### 1. CalendarScreen.ktの修正

#### 1-1. TopAppBarのドロップダウンメニュー修正（行264-346）

**現在の構成**:
```kotlin
DropdownMenu(...) {
    DropdownMenuItem("日") { onDayScheduleClick() }
    DropdownMenuItem("週") { onWeekScheduleClick() }
    DropdownMenuItem("月") { /* 何もしない */ }
}
```

**変更後の構成**:
```kotlin
DropdownMenu(...) {
    DropdownMenuItem("カレンダー") { ... }    // 月間カレンダービュー
    DropdownMenuItem("リスト") { ... }        // 月間リストビュー（新規）
}
```

**表示モード状態管理**:
```kotlin
// 既存の selectedViewMode を変更
var selectedViewMode by remember { mutableStateOf("カレンダー") }

// メニュー選択時の動作
DropdownMenuItem(
    text = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("カレンダー")
            if (selectedViewMode == "カレンダー") {
                Text("✓", fontWeight = FontWeight.Bold)
            }
        }
    },
    onClick = {
        selectedViewMode = "カレンダー"
        viewModeMenuExpanded = false
    }
)
DropdownMenuItem(
    text = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("リスト")
            if (selectedViewMode == "リスト") {
                Text("✓", fontWeight = FontWeight.Bold)
            }
        }
    },
    onClick = {
        selectedViewMode = "リスト"
        viewModeMenuExpanded = false
    }
)
```

**TopAppBarの構造**:
```
┌─────────────────────────────────────┐
│ ≡  2025年10月  [カレンダー▼] 🔗  👤│ ← TopAppBar
│                 ├カレンダー ✓       │
│                 └リスト             │
└─────────────────────────────────────┘
```

#### 1-2. Scaffold内のコンテンツ切り替え（行366-398）

**変更内容**:
```kotlin
Scaffold(...) { paddingValues ->
    // 選択されたモードに応じてコンテンツを切り替え
    when (selectedViewMode) {
        "カレンダー" -> {
            // 既存の月間カレンダービュー
            BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val month = months[page]
                    Column(
                        modifier = Modifier
                            .width(screenWidth)
                            .background(MaterialTheme.colorScheme.surface)
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
        "リスト" -> {
            // 新しい月間リストビュー
            MonthListView(
                focusedMonth = focusedMonth,
                schedules = schedules,
                holidays = holidays,
                userColorMap = userColorMap,
                onClickDate = onClickDate,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
```

#### 1-3. 新しいComposableの追加: MonthListView

**配置場所**: CalendarScreen.kt内のprivate Composable（ファイル末尾）

**シグネチャ**:
```kotlin
@Composable
private fun MonthListView(
    focusedMonth: LocalDate,
    schedules: Map<LocalDate, List<Schedule>>,
    holidays: Map<LocalDate, HolidayRepository.Holiday>,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit,
    modifier: Modifier = Modifier
)
```

**機能**:
- 1ヶ月分（1日〜末日）の日付を縦スクロールリストで表示
- `LazyColumn`を使用
- 各行を2列に分割：
  - **左列（固定幅）**: 日付（曜日、祝日表示含む）
  - **右列（可変幅）**: その日の予定リスト

**レイアウトイメージ**:
```
┌───────────────────────────────────────┐
│ ≡  2025年10月   [リスト▼] 🔗  👤    │ ← TopAppBar (共通)
├───────┬───────────────────────────────┤
│ 1(火)│ ・仕事 9:00-18:00 (あなた)     │
│      │ ・美容院 10:00-12:00 (パートナー)│
├───────┼───────────────────────────────┤
│ 2(水)│ ・会議 14:00-15:00 (あなた)    │
├───────┼───────────────────────────────┤
│ 3(木)│ （予定なし）                   │
├───────┼───────────────────────────────┤
│  ...  │                               │
└───────┴───────────────────────────────┘
```

**実装詳細**:
```kotlin
@Composable
private fun MonthListView(
    focusedMonth: LocalDate,
    schedules: Map<LocalDate, List<Schedule>>,
    holidays: Map<LocalDate, HolidayRepository.Holiday>,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit,
    modifier: Modifier = Modifier
) {
    // 月の1日を取得
    val firstDayOfMonth = LocalDate(focusedMonth.year, focusedMonth.month, 1)

    // 月の日数を取得
    val daysInMonth = when (focusedMonth.month) {
        kotlinx.datetime.Month.JANUARY, kotlinx.datetime.Month.MARCH,
        kotlinx.datetime.Month.MAY, kotlinx.datetime.Month.JULY,
        kotlinx.datetime.Month.AUGUST, kotlinx.datetime.Month.OCTOBER,
        kotlinx.datetime.Month.DECEMBER -> 31
        kotlinx.datetime.Month.APRIL, kotlinx.datetime.Month.JUNE,
        kotlinx.datetime.Month.SEPTEMBER, kotlinx.datetime.Month.NOVEMBER -> 30
        kotlinx.datetime.Month.FEBRUARY -> {
            // 閏年判定
            if ((focusedMonth.year % 4 == 0 && focusedMonth.year % 100 != 0) ||
                (focusedMonth.year % 400 == 0)) 29 else 28
        }
        else -> 31
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(daysInMonth) { index ->
            val dayOfMonth = index + 1
            val date = LocalDate(focusedMonth.year, focusedMonth.month, dayOfMonth)
            val daySchedules = schedules[date] ?: emptyList()
            val holiday = holidays[date]

            DateScheduleRow(
                date = date,
                schedules = daySchedules,
                holiday = holiday,
                userColorMap = userColorMap,
                onClickDate = onClickDate
            )
        }
    }
}

@Composable
private fun DateScheduleRow(
    date: LocalDate,
    schedules: List<Schedule>,
    holiday: HolidayRepository.Holiday?,
    userColorMap: Map<User.Id, UserColor>,
    onClickDate: (Day) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDate(Day(date)) }
            .background(
                when {
                    date.dayOfWeek == kotlinx.datetime.DayOfWeek.SATURDAY ->
                        Color.Blue.copy(alpha = 0.05f)
                    date.dayOfWeek == kotlinx.datetime.DayOfWeek.SUNDAY || holiday != null ->
                        Color.Red.copy(alpha = 0.05f)
                    else -> Color.Transparent
                }
            )
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        // 左列: 日付
        Column(
            modifier = Modifier
                .width(70.dp)
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    holiday != null -> Color.Red
                    date.dayOfWeek == kotlinx.datetime.DayOfWeek.SATURDAY -> Color.Blue
                    date.dayOfWeek == kotlinx.datetime.DayOfWeek.SUNDAY -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = getDayOfWeekJapanese(date.dayOfWeek),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (holiday != null) {
                Text(
                    text = holiday.name,
                    fontSize = 9.sp,
                    color = Color.Red,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        // 右列: 予定リスト
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            if (schedules.isEmpty()) {
                Text(
                    text = "予定なし",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                schedules.sortedBy { it.startDateTime }.forEach { schedule ->
                    ScheduleListItem(
                        schedule = schedule,
                        userColor = userColorMap[schedule.createUser.id] ?: UserColor.default()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ScheduleListItem(
    schedule: Schedule,
    userColor: UserColor
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ユーザー色のインジケーター
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(userColor.value), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // 予定情報
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = schedule.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 時刻表示
                Text(
                    text = when (val time = schedule.time) {
                        is ScheduleTime.SingleTimeDay ->
                            "${time.startTime.hour}:${time.startTime.minute.toString().padStart(2, '0')}-${time.endTime.hour}:${time.endTime.minute.toString().padStart(2, '0')}"
                        is ScheduleTime.DateTimeRange ->
                            "${time.start.time.hour}:${time.start.time.minute.toString().padStart(2, '0')}-${time.end.time.hour}:${time.end.time.minute.toString().padStart(2, '0')}"
                        is ScheduleTime.AllDayRange ->
                            "終日(${time.startDate.dayOfMonth}日-${time.endDate.dayOfMonth}日)"
                        else -> "終日"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // ユーザー名
                Text(
                    text = schedule.createUser.name,
                    fontSize = 12.sp,
                    color = Color(userColor.value),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// 曜日を日本語で取得するヘルパー関数
private fun getDayOfWeekJapanese(dayOfWeek: kotlinx.datetime.DayOfWeek): String {
    return when (dayOfWeek) {
        kotlinx.datetime.DayOfWeek.MONDAY -> "月"
        kotlinx.datetime.DayOfWeek.TUESDAY -> "火"
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> "水"
        kotlinx.datetime.DayOfWeek.THURSDAY -> "木"
        kotlinx.datetime.DayOfWeek.FRIDAY -> "金"
        kotlinx.datetime.DayOfWeek.SATURDAY -> "土"
        kotlinx.datetime.DayOfWeek.SUNDAY -> "日"
    }
}
```

### 2. CalendarScreen.ktのシグネチャ修正

**変更内容**:
- `onWeekScheduleClick`と`onDayScheduleClick`パラメータを削除
- これらのコールバックは使用しなくなるため不要
- `onClickDate`は既に存在（日付クリック時に詳細画面に遷移）

**変更前**:
```kotlin
fun CalendarScreen(
    ...
    onWeekScheduleClick: () -> Unit = {},
    onDayScheduleClick: () -> Unit = {},
    ...
)
```

**変更後**:
```kotlin
fun CalendarScreen(
    ...
    // onWeekScheduleClick と onDayScheduleClick を削除
    ...
)
```

### 3. App.ktの修正

**変更内容**:
CalendarScreenの呼び出しから`onWeekScheduleClick`と`onDayScheduleClick`を削除

**変更前**:
```kotlin
CalendarScreen(
    months = calendarState.months,
    ...
    onWeekScheduleClick = {
        navController.navigate(Screen.WeekSchedule.from(today))
    },
    onDayScheduleClick = {
        navController.navigate(Screen.DaySchedule.from(today))
    },
    ...
)
```

**変更後**:
```kotlin
CalendarScreen(
    months = calendarState.months,
    ...
    // onWeekScheduleClick と onDayScheduleClick を削除
    ...
)
```

**注意**: 週間ビューと日間ビューへの遷移は、カレンダー画面からは行わなくなります。これらの画面へのアクセスは、他の方法（例：日付をクリックして日間ビューへ遷移）で提供されます。

### 4. その他の修正

**変更不要**:
- Screen.kt: 新しい画面定義が不要なため変更なし
- 新規ファイル作成: 不要（CalendarScreen内で完結）

## データフロー

1. **予定データ取得**:
   - CalendarStateが既に保持している`schedules`を使用
   - フィルタリング済みのデータが渡される

2. **祝日データ取得**:
   - CalendarStateが既に保持している`holidays`を使用

3. **ユーザー色情報**:
   - CalendarStateが既に保持している`userColorMap`を使用

## 再利用するコンポーネント

- `CommonTopAppBar`: TopAppBar（既存）
- `DropdownMenu`/`DropdownMenuItem`: 既存のメニューを拡張
- `LazyColumn`: 効率的なリスト表示
- `UserColor`: ユーザーカラーの管理
- `HolidayRepository.Holiday`: 祝日データ構造
- `Day`: 日付モデル（既存）

## 実装の優先順位

1. **Phase 1**: ドロップダウンメニューを「カレンダー」「リスト」の2つに変更
2. **Phase 2**: `selectedViewMode`の初期値を"カレンダー"に変更
3. **Phase 3**: `when (selectedViewMode)`による表示切り替えロジックを実装
4. **Phase 4**: MonthListView Composableの基本構造実装
5. **Phase 5**: DateScheduleRow Composableの実装（日付列）
6. **Phase 6**: ScheduleListItem Composableの実装（予定列）
7. **Phase 7**: CalendarScreenとApp.ktから週・日ビュー関連のコードを削除
8. **Phase 8**: 色分け・アイコン表示とUI調整

## 技術的考慮事項

- **パフォーマンス**:
  - LazyColumnで最大31行のみなので問題なし
  - CalendarStateから直接データを受け取るため追加の取得処理不要

- **状態管理**:
  - 既存の`selectedViewMode`を"カレンダー"と"リスト"の2つに簡素化
  - 画面内で完結するためシンプルな状態管理

- **ユーザー体験**:
  - シンプルな2択のメニューで迷わない
  - カレンダーとリストをスムーズに切り替え可能
  - 日付行全体がクリッカブルで詳細画面に遷移可能
  - 月の切り替えはHorizontalPager（カレンダー）またはTopAppBarの月表示から可能

- **アクセシビリティ**:
  - 色分けに加えてユーザー名も表示することで識別性を向上
  - 曜日・祝日の色分けで視認性向上
  - 予定がない日も明示的に表示

## 利点

1. **シンプルなUI**: カレンダーとリストの2択のみで迷わない
2. **既存UIパターンを踏襲**: ドロップダウンメニューで統一感
3. **コード削減**: 週・日ビューへの遷移コードが不要、新規ファイルも不要
4. **パフォーマンス向上**: 状態切り替えのみで再取得不要
5. **保守性向上**: 関連コードが1ファイルに集約、管理が容易
