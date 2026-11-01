package com.gerstudio.cleanbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.model.SafetyLevel

@Composable
fun SafetyBadge(level: SafetyLevel, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label, icon) = when (level) {
        SafetyLevel.SAFE_TO_REMOVE -> Quad(
            Color(0xFF1B5E20).copy(alpha = 0.15f),
            Color(0xFF2E7D32),
            "Safe to Remove",
            Icons.Default.CheckCircle
        )
        SafetyLevel.CAUTION -> Quad(
            Color(0xFFF57F17).copy(alpha = 0.15f),
            Color(0xFFF57F17),
            "Caution",
            Icons.Default.Warning
        )
        SafetyLevel.PROTECTED_CORE -> Quad(
            Color(0xFFB71C1C).copy(alpha = 0.15f),
            Color(0xFFC62828),
            "Protected Core",
            Icons.Default.Lock
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
