package com.github.mtkw0127.scheduleshare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User

@Composable
fun ScheduleCard(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {},
    showUserIcons: Boolean = false,
    getUserColor: ((User.Id) -> Color)? = null
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
                    Column {
                        Text(
                            text = displayTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // 参加者のアイコンを表示
                        if (showUserIcons && schedule.assignedUsers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                schedule.assignedUsers.take(5).forEach { user ->
                                    UserIcon(
                                        user = user,
                                        color = getUserColor?.invoke(user.id) ?: Color.Gray,
                                        size = 16.dp
                                    )
                                }
                                if (schedule.assignedUsers.size > 5) {
                                    Text(
                                        text = "+${schedule.assignedUsers.size - 5}",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
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
                        // 参加者のアイコンを表示
                        if (showUserIcons && schedule.assignedUsers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                schedule.assignedUsers.take(5).forEach { user ->
                                    UserIcon(
                                        user = user,
                                        color = getUserColor?.invoke(user.id) ?: Color.Gray,
                                        size = 18.dp
                                    )
                                }
                                if (schedule.assignedUsers.size > 5) {
                                    Text(
                                        text = "+${schedule.assignedUsers.size - 5}",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
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

                        // 参加者のアイコンを表示
                        if (showUserIcons && schedule.assignedUsers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                schedule.assignedUsers.take(5).forEach { user ->
                                    UserIcon(
                                        user = user,
                                        color = getUserColor?.invoke(user.id) ?: Color.Gray,
                                        size = 20.dp
                                    )
                                }
                                if (schedule.assignedUsers.size > 5) {
                                    Text(
                                        text = "+${schedule.assignedUsers.size - 5}",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserIcon(
    user: User,
    color: Color,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // iconUrlがnullの場合は名前の最初の文字を表示
        val displayText = user.name.firstOrNull()?.toString() ?: "?"
        Text(
            text = displayText,
            fontSize = (size.value * 0.5).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(0.dp),
            lineHeight = (size.value * 0.5).sp
        )
    }
}