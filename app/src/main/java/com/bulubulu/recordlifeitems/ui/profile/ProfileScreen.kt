package com.bulubulu.recordlifeitems.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.ui.components.ProjectIcon
import com.bulubulu.recordlifeitems.ui.projects.ProjectsViewModel
import com.bulubulu.recordlifeitems.data.entity.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProjectDetail: (Long) -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val deletedProjects by viewModel.deletedProjects.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar
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
            var showAbout by remember { mutableStateOf(false) }
            Card(modifier = Modifier.fillMaxWidth().clickable { showAbout = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "\u5173\u4e8e", style = MaterialTheme.typography.titleMedium)
                        Text(text = "\u7248\u672c\u4fe1\u606f\u4e0e\u7248\u672c\u66f4\u65b0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = "v1.002", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (showAbout) {
                AlertDialog(
                    onDismissRequest = { showAbout = false },
                    title = { Text("\u5173\u4e8e") },
                    text = {
                        Column {
                            Text("\u5e94\u7528\u540d\u79f0\uff1a\u8bb0\u5f55\u751f\u6d3b")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("\u5f53\u524d\u7248\u672c\uff1av1.002")
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
                            Text("\u2022 \u4fee\u590d\u989c\u8272\u9009\u62e9\u95ee\u9898")
                            Text("\u2022 \u4fee\u590d\u6eda\u8f6e\u9009\u62e9\u5668")
                        }
                    },
                    confirmButton = { TextButton(onClick = { showAbout = false }) { Text("\u786e\u5b9a") } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedProjectsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val deletedProjects by viewModel.deletedProjects.collectAsState()

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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding() + 8.dp, bottom = paddingValues.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = deletedProjects, key = { it.id }) { project ->
                    DeletedProjectItem(project = project, onRestore = { viewModel.restoreProject(project) })
                }
            }
        }
    }
}

@Composable
private fun DeletedProjectItem(project: Project, onRestore: () -> Unit) {
    var showRestoreDialog by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProjectIcon(iconName = project.icon, color = Color(project.color.toInt()), size = 36)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = project.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (project.description.isNotBlank()) { Text(text = project.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { showRestoreDialog = true }) { Text("\u6062\u590d") }
        }
    }
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("\u6062\u590d\u9879\u76ee") },
            text = { Text("\u786e\u5b9a\u8981\u6062\u590d\u300c${project.name}\u300d\u5417\uff1f") },
            confirmButton = { TextButton(onClick = { onRestore(); showRestoreDialog = false }) { Text("\u6062\u590d") } },
            dismissButton = { TextButton(onClick = { showRestoreDialog = false }) { Text("\u53d6\u6d88") } }
        )
    }
}
