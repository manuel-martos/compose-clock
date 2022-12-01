package com.playzinkin.composeclock.utils

import kotlin.random.Random

fun Random.nextFloat(start: Float, end: Float) = start + nextFloat() * (end - start)
