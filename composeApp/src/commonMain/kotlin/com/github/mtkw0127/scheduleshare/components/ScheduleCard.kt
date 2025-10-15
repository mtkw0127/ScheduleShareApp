package com.github.mtkw0127.scheduleshare.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule

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