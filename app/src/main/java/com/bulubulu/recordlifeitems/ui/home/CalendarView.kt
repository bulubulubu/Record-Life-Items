package com.bulubulu.recordlifeitems.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bulubulu.recordlifeitems.ui.components.ColorIndicator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    days: List<CalendarDay>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Day of week headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dayOfWeekNames = listOf("一", "二", "三", "四", "五", "六", "日")
            dayOfWeekNames.forEach { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Calendar grid
        val rows = days.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        onClick = { onDayClick(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        day.isToday -> MaterialTheme.colorScheme.primary
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val backgroundColor = when {
        day.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderModifier = if (day.isToday) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .aspectRatio(0.85f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(borderModifier)
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Date number
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textAlign = TextAlign.Center,
            fontSize = if (day.isToday) 14.sp else 13.sp
        )

        // Color indicator dots for check-ins
        if (day.checkIns.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                val uniqueColors = day.checkIns
                    .map { it.project.color }
                    .distinct()
                    .take(4) // Max 4 dots

                uniqueColors.forEach { colorLong ->
                    val dotColor = if (colorLong.toInt() == 0) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color(colorLong.toInt())
                    ColorIndicator(
                        color = dotColor,
                        size = 5.dp,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
            }
        }
    }
}
