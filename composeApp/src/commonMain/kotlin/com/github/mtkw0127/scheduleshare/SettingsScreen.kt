package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mtkw0127.scheduleshare.components.CommonTopAppBar
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back
import scheduleshare.composeapp.generated.resources.user

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CommonTopAppBar(
                title = {
                    Text(
                        text = "設定",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                imageVector = vectorResource(Res.drawable.user),
                contentDescription = "ユーザー",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "テストユーザー",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ログアウト")
            }
        }
    }
}
