package com.bulubulu.recordlifeitems.ui.projects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.ui.components.ProjectIcon
import com.bulubulu.recordlifeitems.data.entity.Project
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigateToProjectDetail: (Long) -> Unit,
    onNavigateToNewProject: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val projects by viewModel.allProjects.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "\u9879\u76ee", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToNewProject() }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "\u6dfb\u52a0", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding() + 8.dp, bottom = paddingValues.calculateBottomPadding() + 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items = projects, key = { _, p -> p.id }) { _, project ->
                SwipeableItem(
                    project = project,
                    onDelete = { viewModel.softDelete(project) },
                    onClick = { onNavigateToProjectDetail(project.id) }
                )
            }
        }
    }
}

@Composable
private fun SwipeableItem(
    project: Project,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val deleteButtonWidth = 120f
    val scope = rememberCoroutineScope()
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, animationSpec = tween(200), label = "offset")

    Box(modifier = Modifier.fillMaxWidth()) {
        // Delete button behind the card
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteButtonWidth.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFF1744))
                .clickable {
                    onDelete()
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "\u5220\u9664", tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("\u5220\u9664", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }

        // Foreground card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -deleteButtonWidth / 2) {
                                // Snap to show delete button
                                scope.launch {
                                    offsetX = -deleteButtonWidth
                                }
                            } else {
                                // Snap back
                                scope.launch {
                                    offsetX = 0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-deleteButtonWidth, 0f)
                        }
                    )
                }
                .clickable {
                    if (offsetX < 0) {
                        scope.launch {
                            offsetX = 0f
                        }
                    } else {
                        onClick()
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ProjectIcon(iconName = project.icon, color = Color(project.color.toInt()), size = 36)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = project.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val s = getScheduleText(project)
                    if (s.isNotBlank()) { Text(text = s, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    else if (project.description.isNotBlank()) { Text(text = project.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            }
        }
    }
}

private fun getScheduleText(project: Project): String {
    val parts = mutableListOf<String>()
    val w = project.weekDays
    if (!w.isNullOrBlank() && w != "[]") {
        try {
            val arr = JSONArray(w)
            val days = (0 until arr.length()).map { arr.getInt(it) }
            if (days.isNotEmpty()) {
                val names = days.map { d -> when(d){1->"\u4e00";2->"\u4e8c";3->"\u4e09";4->"\u56db";5->"\u4e94";6->"\u516d";7->"\u65e5";else->""} }.filter{it.isNotBlank()}
                if (names.size==7) parts.add("\u6bcf\u65e5") else if(names.isNotEmpty()) parts.add("\u5468${names.joinToString("")}")
            }
        } catch(e: Exception){}
    }
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val s = project.startDate; val e = project.endDate
    if (s!=null && e!=null) parts.add("${df.format(Date(s))} ~ ${df.format(Date(e))}")
    else if (s!=null) parts.add("${df.format(Date(s))} \u8d77")
    else if (e!=null) parts.add("\u81f3 ${df.format(Date(e))}")
    return parts.joinToString(" \u00b7 ")
}
