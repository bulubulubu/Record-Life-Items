package com.bulubulu.recordlifeitems.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

object ProjectIcons {
    data class IconItem(val name: String, val vector: ImageVector)

    val icons = listOf(
        IconItem("star", Icons.Default.Star),
        IconItem("favorite", Icons.Default.Favorite),
        IconItem("home", Icons.Default.Home),
        IconItem("code", Icons.Default.Code),
        IconItem("chat", Icons.Default.Chat),
        IconItem("music", Icons.Default.MusicNote),
        IconItem("school", Icons.Default.School),
        IconItem("flight", Icons.Default.Flight),
        IconItem("restaurant", Icons.Default.Restaurant),
        IconItem("shopping", Icons.Default.ShoppingCart),
        IconItem("pets", Icons.Default.Pets),
        IconItem("park", Icons.Default.Park),
        IconItem("game", Icons.Default.SportsEsports),
        IconItem("fitness", Icons.Default.FitnessCenter),
        IconItem("work", Icons.Default.Work),
        IconItem("camera", Icons.Default.CameraAlt),
        IconItem("health", Icons.Default.HealthAndSafety),
        IconItem("beach", Icons.Default.BeachAccess),
        IconItem("magic", Icons.Default.AutoAwesome),
        IconItem("bolt", Icons.Default.Bolt),
    )

    fun getIcon(name: String?): ImageVector {
        return icons.find { it.name == name }?.vector ?: Icons.Default.Star
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectIconPicker(
    selectedIcon: String?,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图标") },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectIcons.icons.forEach { iconItem ->
                    val isSelected = iconItem.name == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .then(
                                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier
                            )
                            .clickable {
                                onIconSelected(iconItem.name)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconItem.vector,
                            contentDescription = iconItem.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ProjectIcon(
    iconName: String?,
    color: Color,
    size: Int = 36,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ProjectIcons.getIcon(iconName),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size((size * 0.55f).dp)
        )
    }
}
