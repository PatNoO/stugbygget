package com.example.stugbygget.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val StugbyggetShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // Badges, chips
    small = RoundedCornerShape(12.dp),       // Small buttons, input fields
    medium = RoundedCornerShape(16.dp),      // Cards, dialogs
    large = RoundedCornerShape(20.dp),       // Large cards, bottom sheets
    extraLarge = RoundedCornerShape(28.dp),  // FAB, hero cards
)

object SommarShapes {
    val button = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(18.dp)
    val headerCard = RoundedCornerShape(20.dp)
    val bottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val badge = RoundedCornerShape(20.dp)
    val progressBar = RoundedCornerShape(4.dp)
    val tabIndicator = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
    val searchBar = RoundedCornerShape(16.dp)
    val chatBubbleUser = RoundedCornerShape(18.dp, 6.dp, 18.dp, 18.dp)
    val chatBubbleAi = RoundedCornerShape(6.dp, 18.dp, 18.dp, 18.dp)
    val timelineIcon = RoundedCornerShape(12.dp)
    val thumbnail = RoundedCornerShape(14.dp)
}
