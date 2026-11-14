package com.triprecap.mockup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object C {
    val Bg = Color(0xFF121614)
    val Surface = Color(0xFF1B201D)
    val Surface2 = Color(0xFF242A26)
    val Line = Color(0xFF323A35)
    val Text = Color(0xFFECEFEC)
    val Sub = Color(0xFF8E9891)
    val Green = Color(0xFF3CC47C)
    val GreenDim = Color(0xFF1E4430)
    val Yellow = Color(0xFFF5C451)
    val Water = Color(0xFF15303C)
    val MapBg = Color(0xFF1A1F1C)
}

@Composable
fun T(
    text: String,
    size: TextUnit = 16.sp,
    color: Color = C.Text,
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
) = Text(text, modifier = modifier, fontSize = size, color = color, fontWeight = weight)

@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(C.Surface)
    ) { content() }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = C.Text) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(C.Surface2)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        T(label, 13.sp, C.Sub)
        Spacer(Modifier.height(4.dp))
        T(value, 22.sp, valueColor, FontWeight.Bold)
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(C.Green)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) { T(text, 16.sp, Color(0xFF0E1A13), FontWeight.Bold) }
}

@Composable
fun OutlineButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, C.Line, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) { T(text, 16.sp, C.Text, FontWeight.Medium) }
}

@Composable
fun StatusPill(text: String, dotColor: Color = C.Green) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(C.Surface2)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        T(text, 14.sp, C.Text)
    }
}

@Composable
fun NumberBadge(n: Int, size: Int = 26) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(C.Yellow),
        contentAlignment = Alignment.Center
    ) { T("$n", (size * 0.5).sp, Color(0xFF2A2006), FontWeight.Bold) }
}

@Composable
fun HSpace(w: Int) = Spacer(Modifier.width(w.dp))

@Composable
fun VSpace(h: Int) = Spacer(Modifier.height(h.dp))
