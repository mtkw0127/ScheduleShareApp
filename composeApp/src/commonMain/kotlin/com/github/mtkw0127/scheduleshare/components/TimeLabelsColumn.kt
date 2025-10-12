package com.github.mtkw0127.scheduleshare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimeLabelsColumn() {
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
