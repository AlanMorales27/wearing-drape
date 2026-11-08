package com.wearingdrape.app.ui

import androidx.compose.ui.graphics.Color

/** "#RRGGBB" de la paleta → Color opaco. */
fun colorFromHex(hex: String): Color =
    Color(hex.removePrefix("#").toLong(16) or 0xFF000000)
