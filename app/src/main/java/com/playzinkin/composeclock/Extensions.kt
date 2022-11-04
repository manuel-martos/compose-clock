package com.playzinkin.composeclock

import kotlin.random.Random

fun Random.nextFloat(start: Float, end: Float) = start + nextFloat() * (end - start)
