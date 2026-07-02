package com.bulubulu.recordlifeitems.ui.projects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.ui.components.ProjectIcon
import com.bulubulu.recordlifeitems.data.entity.Project
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectsScreen(
    onNavigateToProjectDetail: (Long) -> Unit,
    onNavigateToNewProject: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val projects by viewModel.allProjects.collectAsState()
    var currentlySwipedId by remember { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val spacingPx = with(density) { 8.dp.toPx() }

    // Local list for immediate updates during drag
    var localItems by remember { mutableStateOf<List<Project>>(emptyList()) }
    // Sync with database when not dragging
    LaunchedEffect(projects) {
        localItems = projects
    }

    // Drag state
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var itemHeightPx by remember { mutableFloatStateOf(0f) }

    // Close swiped item when scrolling
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            currentlySwipedId = null
        }
    }

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
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding() + 8.dp, bottom = paddingValues.calculateBottomPadding() + 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items = localItems, key = { _, p -> p.id }) { index, project ->
                val isDragging = draggedIndex == index
                val visualOffsetY = if (isDragging) dragOffsetY else 0f

                ProjectCard(
                    project = project,
                    isSwiped = currentlySwipedId == project.id && !isDragging,
                    isDragging = isDragging,
                    offsetY = visualOffsetY,
                    onSwipeStart = { currentlySwipedId = project.id },
                    onSwipeCancel = { if (currentlySwipedId == project.id) currentlySwipedId = null },
                    onDelete = {
                        currentlySwipedId = null
                        viewModel.softDelete(project)
                    },
                    onClick = {
                        if (currentlySwipedId != null) {
                            currentlySwipedId = null
                        } else if (draggedIndex == -1) {
                            onNavigateToProjectDetail(project.id)
                        }
                    },
                    onDragStart = {
                        currentlySwipedId = null
                        draggedIndex = index
                        dragOffsetY = 0f
                    },
                    onDrag = { deltaY ->
                        dragOffsetY += deltaY

                        if (itemHeightPx > 0) {
                            val totalItemSize = itemHeightPx + spacingPx
                            val threshold = totalItemSize / 2f

                            // Moving DOWN
                            while (dragOffsetY > threshold && draggedIndex < localItems.size - 1) {
                                val newList = localItems.toMutableList()
                                val item = newList.removeAt(draggedIndex)
                                newList.add(draggedIndex + 1, item)
                                localItems = newList
                                draggedIndex += 1
                                dragOffsetY -= totalItemSize
                            }
                            // Moving UP
                            while (dragOffsetY < -threshold && draggedIndex > 0) {
                                val newList = localItems.toMutableList()
                                val item = newList.removeAt(draggedIndex)
                                newList.add(draggedIndex - 1, item)
                                localItems = newList
                                draggedIndex -= 1
                                dragOffsetY += totalItemSize
                            }
                        }
                    },
                    onDragEnd = {
                        draggedIndex = -1
                        dragOffsetY = 0f
                        viewModel.saveReorder(localItems)
                    },
                    onItemHeightChanged = { itemHeightPx = it.toFloat() },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    isSwiped: Boolean,
    isDragging: Boolean,
    offsetY: Float,
    onSwipeStart: () -> Unit,
    onSwipeCancel: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onItemHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val deleteButtonWidthDp = 80
    val deleteButtonWidthPx = with(LocalDensity.current) { deleteButtonWidthDp.dp.toPx() }
    var swipeOffsetX by remember { mutableFloatStateOf(0f) }
    var cardHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val targetSwipeX = if (isSwiped) -deleteButtonWidthPx else 0f
    val animatedSwipeX by animateFloatAsState(targetValue = targetSwipeX, animationSpec = tween(200), label = "swipe")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .zIndex(if (isDragging) 1f else 0f)
            .onSizeChanged { size ->
                cardHeight = size.height
                onItemHeightChanged(size.height)
            }
            // All gesture detection on the outer Box for correct touch position
            .pointerInput(project.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { _, dragAmount -> onDrag(dragAmount.y) }
                )
            }
    ) {
        // Delete button behind the card
        if (isSwiped && cardHeight > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(deleteButtonWidthDp.dp)
                    .height(with(density) { cardHeight.toDp() })
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF1744))
                    .clickable { onDelete() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "\u5220\u9664", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        // Foreground card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedSwipeX.roundToInt(), 0) }
                .shadow(if (isDragging) 8.dp else 0.dp, RoundedCornerShape(12.dp))
                .pointerInput(project.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (swipeOffsetX < -deleteButtonWidthPx * 0.3f) onSwipeStart() else onSwipeCancel()
                            swipeOffsetX = 0f
                        },
                        onDragCancel = { swipeOffsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            if (dragAmount < 0 || swipeOffsetX < 0) {
                                swipeOffsetX = (swipeOffsetX + dragAmount).coerceIn(-deleteButtonWidthPx, 0f)
                            }
                        }
                    )
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (isSwiped) onSwipeCancel() else onClick()
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 4.dp else 1.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val iconColor = if (project.color.toInt() == 0) MaterialTheme.colorScheme.primary else Color(project.color.toInt())
                ProjectIcon(iconName = project.icon, color = iconColor, size = 36)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = project.name, style = MaterialTheme.typography.titleMedium, color = iconColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
