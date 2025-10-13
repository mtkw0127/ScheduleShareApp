package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.github.mtkw0127.scheduleshare.model.user.User
import org.jetbrains.compose.resources.vectorResource
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.arrow_back

@Composable
fun QRShareScreen(
    onBackClick: () -> Unit = {}
) {
    val userId = User.createTest().id.value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CommonTopAppBar(
                title = {
                    Text(
                        text = "QRコードで共有",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "相手のカメラで読み取ってもらい、\n予定を共有しましょう",
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // QRコードプレースホルダー
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "QRコード\n\n$userId",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "※ QRコードを読み取ると、\nお互いの予定が見えるようになります",
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
