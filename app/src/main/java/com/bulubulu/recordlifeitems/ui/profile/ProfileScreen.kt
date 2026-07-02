package com.bulubulu.recordlifeitems.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.ui.components.ProjectIcon
import com.bulubulu.recordlifeitems.ui.projects.ProjectsViewModel
import com.bulubulu.recordlifeitems.data.entity.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity

// Version is read dynamically from PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProjectDetail: (Long) -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val deletedProjects by viewModel.deletedProjects.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAbout by remember { mutableStateOf(false) }
    val updateViewModel: UpdateViewModel = viewModel()
    val isDownloading by updateViewModel.isDownloading.collectAsState()
    val downloadProgress by updateViewModel.downloadProgress.collectAsState()
    val statusMessage by updateViewModel.statusMessage.collectAsState()
    val isChecking by updateViewModel.isChecking.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "\u8bb0\u5f55\u751f\u6d3b", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))

            // Deleted projects
            Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToProjectDetail(-1) }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RestoreFromTrash, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "\u5df2\u5220\u9664\u7684\u6d3b\u52a8", style = MaterialTheme.typography.titleMedium)
                        Text(text = "\u67e5\u770b\u5e76\u6062\u590d\u5df2\u5220\u9664\u7684\u9879\u76ee", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = deletedProjects.size.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // About
            Card(modifier = Modifier.fillMaxWidth().clickable { showAbout = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "\u5173\u4e8e", style = MaterialTheme.typography.titleMedium)
                        Text(text = "\u7248\u672c\u4fe1\u606f\u4e0e\u7248\u672c\u66f4\u65b0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = "v${UpdateViewModel.getCurrentVersion(context)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Download progress
            if (isDownloading) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(text = "\u6b63\u5728\u4e0b\u8f7d\u66f4\u65b0...", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { downloadProgress }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${(downloadProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("\u5173\u4e8e")
                    IconButton(onClick = {
                        updateViewModel.checkAndUpdate(context)
                    }) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = "\u68c0\u67e5\u66f4\u65b0")
                        }
                    }
                }
            },
            text = {
                Column {
                    Text("\u5e94\u7528\u540d\u79f0\uff1a\u8bb0\u5f55\u751f\u6d3b")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("当前版本：v${UpdateViewModel.getCurrentVersion(context)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("\u529f\u80fd\u8bf4\u660e\uff1a")
                    Text("\u2022 \u65e5\u5386\u6253\u5361")
                    Text("\u2022 \u9879\u76ee\u7ba1\u7406")
                    Text("\u2022 \u6d3b\u52a8\u8bb0\u5f55")
                    Text("\u2022 \u56fe\u6807\u4e0e\u989c\u8272\u81ea\u5b9a\u4e49")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("\u66f4\u65b0\u65e5\u5fd7\uff1a", style = MaterialTheme.typography.labelLarge)
                    Text("\u2022 \u65b0\u589e\u5de6\u6ed1\u5220\u9664\u6d3b\u52a8")
                    Text("\u2022 \u65b0\u589e\u4e2a\u4eba\u9875\u9762")
                    Text("\u2022 \u65b0\u589e\u56fe\u6807\u9009\u62e9")
                }
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("\u5173\u95ed") } }
        )
    }
}



private fun getCurrentVersion(context: android.content.Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "unknown"
    } catch (e: Exception) { "unknown" }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedProjectsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val deletedProjects by viewModel.deletedProjects.collectAsState()
    var currentlySwipedId by remember { mutableStateOf<Long?>(null) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Close swiped item when scrolling
    androidx.compose.runtime.LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && currentlySwipedId != null) {
            currentlySwipedId = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u5df2\u5220\u9664\u7684\u6d3b\u52a8") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "\u8fd4\u56de") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (deletedProjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = "\u6ca1\u6709\u5df2\u5220\u9664\u7684\u6d3b\u52a8", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding() + 8.dp, bottom = paddingValues.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = deletedProjects, key = { it.id }) { project ->
                    DeletedProjectItem(
                        project = project,
                        isSwiped = currentlySwipedId == project.id,
                        onSwipeStart = { currentlySwipedId = project.id },
                        onSwipeCancel = { if (currentlySwipedId == project.id) currentlySwipedId = null },
                        onRestore = { viewModel.restoreProject(project) },
                        onDelete = { currentlySwipedId = null; viewModel.deleteProject(project) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeletedProjectItem(
    project: Project,
    isSwiped: Boolean,
    onSwipeStart: () -> Unit,
    onSwipeCancel: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val deleteButtonWidthDp = 80
    val density = LocalDensity.current
    val deleteButtonWidthPx = with(density) { deleteButtonWidthDp.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var cardHeight by remember { mutableStateOf(0) }
    val targetOffset = if (isSwiped) -deleteButtonWidthPx else 0f
    val animatedOffsetX by animateFloatAsState(targetValue = targetOffset, animationSpec = tween(200), label = "offset")

    Box(modifier = Modifier.fillMaxWidth()) {
        if (isSwiped && cardHeight > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(deleteButtonWidthDp.dp)
                    .height(with(density) { cardHeight.toDp() })
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF1744))
                    .clickable { showDeleteDialog = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .onSizeChanged { cardHeight = it.height }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -deleteButtonWidthPx * 0.3f) {
                                onSwipeStart()
                            } else {
                                onSwipeCancel()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            if (dragAmount < 0 || offsetX < 0) {
                                offsetX = (offsetX + dragAmount).coerceIn(-deleteButtonWidthPx, 0f)
                            }
                        }
                    )
                }
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    if (isSwiped) onSwipeCancel()
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ProjectIcon(iconName = project.icon, color = Color(project.color.toInt()), size = 36)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = project.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (project.description.isNotBlank()) { Text(text = project.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { showRestoreDialog = true }) { Text("恢复") }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("恢复项目") },
            text = { Text("确定要恢复此项目吗？") },
            confirmButton = { TextButton(onClick = { onRestore(); showRestoreDialog = false }) { Text("恢复") } },
            dismissButton = { TextButton(onClick = { showRestoreDialog = false }) { Text("取消") } }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("永久删除") },
            text = { Text("确定要永久删除此项目吗？此操作不可撤销。") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("删除", color = Color(0xFFFF1744)) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }
}

