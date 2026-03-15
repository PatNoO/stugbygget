package com.example.stugbygget.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Warm shadow modifiers ──

fun Modifier.warmShadow(
    elevation: Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = StugbyggetShapes.medium,
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = Color(0x0D2C1810),
    spotColor = Color(0x1A2C1810),
)

fun Modifier.cardShadow(): Modifier = warmShadow(elevation = 2.dp, shape = SommarShapes.card)
fun Modifier.headerShadow(): Modifier = warmShadow(elevation = 6.dp, shape = SommarShapes.headerCard)
fun Modifier.buttonShadow(): Modifier = warmShadow(elevation = 4.dp, shape = SommarShapes.button)

// ── Gradient brushes per module ──

object SommarGradients {
    val faluRed = Brush.linearGradient(listOf(FaluRed, FaluRedLight))
    val lakeBlue = Brush.linearGradient(listOf(LakeBlue, LakeBlueLight))
    val meadowGreen = Brush.linearGradient(listOf(MeadowGreen, MeadowGreenLight))
    val midsummerGold = Brush.linearGradient(listOf(MidsummerGold, MidsummerGoldLight))
    val woodWarm = Brush.linearGradient(listOf(WoodWarm, WoodWarmLight))

    // Diagonal variants for hero cards
    val faluRedDiagonal = Brush.linearGradient(
        listOf(FaluRed, FaluRedLight),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    val lakeBlueDiagonal = Brush.linearGradient(
        listOf(LakeBlue, LakeBlueLight),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
}
