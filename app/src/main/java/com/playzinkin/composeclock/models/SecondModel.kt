package com.playzinkin.composeclock.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


data class SecondModel(
    val style: Style,
    val state: State
) {
    sealed class Style {
        abstract val size: Dp
        abstract val color: Color

        object Normal : Style() {
            override val size: Dp = 4.dp
            override val color: Color = Color.White
        }
    }

    data class State(
        val seconds: Long = 0,
        val millis: Long = 0,
    )

    companion object
}
