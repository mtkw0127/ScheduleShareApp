package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import com.github.mtkw0127.scheduleshare.extension.toJapanese
import com.github.mtkw0127.scheduleshare.extension.toYmd
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.util.rememberClipboardManager
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back
import scheduleshare.composeapp.generated.resources.copy
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
@Composable
fun ScheduleAddScreen(
    date: LocalDate,
    scheduleRepository: ScheduleRepository,
    scheduleId: String? = null,
    initialStartHour: Int? = null,
    initialStartMinute: Int? = null,
    initialEndHour: Int? = null,
    initialEndMinute: Int? = null,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    // 既存の予定を取得
    val existingSchedule = remember(scheduleId) {
        scheduleId?.let { scheduleRepository.getScheduleById(Schedule.Id(it)) }
    }

    val clipboardManager = rememberClipboardManager()

    var title by remember { mutableStateOf(existingSchedule?.title ?: "") }
    var description by remember { mutableStateOf(existingSchedule?.description ?: "") }

    // 初期時刻が指定されている場合は時間指定モードで開始
    var isAllDay by remember {
        mutableStateOf(
            if (initialStartHour != null) {
                false
            } else {
                existingSchedule?.timeType is Schedule.TimeType.AllDay || existingSchedule == null
            }
        )
    }

    val initialStartTime = (existingSchedule?.timeType as? Schedule.TimeType.Timed)?.start
    val initialEndTime = (existingSchedule?.timeType as? Schedule.TimeType.Timed)?.end

    var startHour by remember {
        mutableStateOf(
            initialStartHour?.toString()?.padStart(2, '0')
                ?: initialStartTime?.hour?.toString()?.padStart(2, '0')
                ?: "09"
        )
    }
    var startMinute by remember {
        mutableStateOf(
            initialStartMinute?.toString()?.padStart(2, '0')
                ?: initialStartTime?.minute?.toString()?.padStart(2, '0')
                ?: "00"
        )
    }
    var endHour by remember {
        mutableStateOf(
            initialEndHour?.toString()?.padStart(2, '0')
                ?: initialEndTime?.hour?.toString()?.padStart(2, '0')
                ?: "10"
        )
    }
    var endMinute by remember {
        mutableStateOf(
            initialEndMinute?.toString()?.padStart(2, '0')
                ?: initialEndTime?.minute?.toString()?.padStart(2, '0')
                ?: "00"
        )
    }

    // 保存ボタンの有効/無効を判定
    val isSaveEnabled = remember(title, isAllDay, startHour, startMinute, endHour, endMinute) {
        if (title.isBlank()) {
            false
        } else if (isAllDay) {
            true
        } else {
            // 時間指定の場合は、時刻が妥当かつ開始 <= 終了であることを確認
            val startH = startHour.toIntOrNull()
            val startM = startMinute.toIntOrNull()
            val endH = endHour.toIntOrNull()
            val endM = endMinute.toIntOrNull()

            if (startH == null || startM == null || endH == null || endM == null) {
                false
            } else if (startH !in 0..23 || startM !in 0..59 || endH !in 0..23 || endM !in 0..59) {
                false
            } else {
                val startTime = LocalTime(startH, startM)
                val endTime = LocalTime(endH, endM)
                startTime <= endTime
            }
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = {
                    Text(
                        text = if (existingSchedule != null) {
                            "${date.toYmd()} (${date.dayOfWeek.toJapanese()}) 予定詳細"
                        } else {
                            "${date.toYmd()} (${date.dayOfWeek.toJapanese()}) 予定追加"
                        },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // タイトル入力欄（既存の予定の場合はコピーアイコン付き）
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル") },
                    modifier = Modifier.weight(1f)
                )
                if (existingSchedule != null && title.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(title)
                        }
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.copy),
                            contentDescription = "タイトルをコピー",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 終日/時間指定の選択
            Text(
                text = "時間",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAllDay = true }
            ) {
                RadioButton(
                    selected = isAllDay,
                    onClick = { isAllDay = true }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("終日")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAllDay = false }
            ) {
                RadioButton(
                    selected = !isAllDay,
                    onClick = { isAllDay = false }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("時間指定")
            }

            // 時間指定の場合の入力欄
            if (!isAllDay) {
                Spacer(modifier = Modifier.height(16.dp))

                // 開始時刻
                Text(
                    text = "開始時刻",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty()) {
                                startHour = newValue
                            } else {
                                val num = newValue.toIntOrNull()
                                if (num != null && num in 0..23 && newValue.length <= 2) {
                                    startHour = newValue
                                }
                            }
                        },
                        label = { Text("時") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(":")
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = startMinute,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty()) {
                                startMinute = newValue
                            } else {
                                val num = newValue.toIntOrNull()
                                if (num != null && num in 0..59 && newValue.length <= 2) {
                                    startMinute = newValue
                                }
                            }
                        },
                        label = { Text("分") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 終了時刻
                Text(
                    text = "終了時刻",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty()) {
                                endHour = newValue
                            } else {
                                val num = newValue.toIntOrNull()
                                if (num != null && num in 0..23 && newValue.length <= 2) {
                                    endHour = newValue
                                }
                            }
                        },
                        label = { Text("時") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(":")
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endMinute,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty()) {
                                endMinute = newValue
                            } else {
                                val num = newValue.toIntOrNull()
                                if (num != null && num in 0..59 && newValue.length <= 2) {
                                    endMinute = newValue
                                }
                            }
                        },
                        label = { Text("分") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 詳細入力欄（既存の予定の場合はコピーアイコン付き）
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "詳細",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (existingSchedule != null && description.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(description)
                            }
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.copy),
                                contentDescription = "詳細をコピー",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("詳細") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // 予定を作成または更新
                    val schedule = if (isAllDay) {
                        Schedule.createAllDay(
                            id = existingSchedule?.id ?: Schedule.Id(
                                TimeSource.Monotonic.markNow().toString()
                            ),
                            title = title,
                            description = description,
                            date = date,
                            user = existingSchedule?.user ?: User.createTest()
                        )
                    } else {
                        val startH = startHour.toInt()
                        val startM = startMinute.toInt()
                        val endH = endHour.toInt()
                        val endM = endMinute.toInt()

                        Schedule.createTimed(
                            id = existingSchedule?.id ?: Schedule.Id(
                                TimeSource.Monotonic.markNow().toString()
                            ),
                            title = title,
                            description = description,
                            date = date,
                            user = existingSchedule?.user ?: User.createTest(),
                            startTime = LocalTime(startH, startM),
                            endTime = LocalTime(endH, endM)
                        )
                    }

                    // Repositoryに追加または更新
                    if (existingSchedule != null) {
                        scheduleRepository.updateSchedule(schedule)
                    } else {
                        scheduleRepository.addSchedule(schedule)
                    }

                    // 画面を閉じる
                    onSaveClick()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isSaveEnabled
            ) {
                Text("保存")
            }
        }
    }
}
