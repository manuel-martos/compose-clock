package com.playzinkin.composeclock.models


fun SecondModel.next(period: Long): SecondModel =
    copy(
        state = SecondModel.State(
            seconds = (state.seconds + ((state.millis + period) / 1000)) % 60,
            millis = (state.millis + period) % 1000,
        )
    )

fun SecondModel.Companion.create(style: SecondModel.Style = SecondModel.Style.Normal) =
    System.currentTimeMillis().let {
        SecondModel(
            style = style,
            state = SecondModel.State(
                seconds = (it / 1000) % 60,
                millis = (it % 1000)
            )
        )
    }