package com.github.mtkw0127.scheduleshare

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.ComposeUIViewController

@OptIn(ExperimentalMaterial3Api::class)
fun MainViewController() = ComposeUIViewController { App() }