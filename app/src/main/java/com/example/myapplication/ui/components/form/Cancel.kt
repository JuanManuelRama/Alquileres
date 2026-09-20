package com.example.myapplication.ui.components.form

import androidx.compose.ui.graphics.vector.ImageVector

data class Cancel(
    val text: String,
    val onClick: () -> Unit,
    val requiresValidForm: Boolean = false,
    val icon: ImageVector? = null
)
