package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar

@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = { Text("設定") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("設定画面")
        }
    }
}
