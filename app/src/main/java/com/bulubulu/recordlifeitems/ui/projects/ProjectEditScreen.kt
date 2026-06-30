package com.bulubulu.recordlifeitems.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.ui.theme.ProjectColors
import com.bulubulu.recordlifeitems.ui.components.ProjectIconPicker
import com.bulubulu.recordlifeitems.ui.components.ProjectIcon
import com.bulubulu.recordlifeitems.ui.components.WheelDatePicker
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProjectEditViewModel = viewModel()
) {
    val project by viewModel.project.collectAsState()
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedWeekdays by viewModel.selectedWeekdays.collectAsState()
    val fields by viewModel.fields.collectAsState()
    val selectedIcon by viewModel.selectedIcon.collectAsState()
    var showIconPicker by remember { mutableStateOf(false) }
    val isNewProject by viewModel.isNewProject.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNewProject) "新建项目" else "编辑项目",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Project Name
            Text(
                text = "项目名称",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("项目名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = "描述",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("描述（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Color Picker
            Text(
                text = "选择颜色",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProjectColors.forEach { color ->
                    val colorLong = color.value.toLong()
                    // Normalize: compare as Int to handle both -1090713120L and 4283215696L
                    val isSelected = color == Color(selectedColor.toInt())
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { viewModel.updateColor(color.value.toLong()) }
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Picker
            Text(
                text = "选择图标",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProjectIcon(
                    iconName = selectedIcon,
                    color = Color(selectedColor.toInt()),
                    size = 48
                )

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(onClick = { showIconPicker = true }) {
                    Text(if (selectedIcon != null) "更换图标" else "选择图标")
                }

                if (selectedIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.updateIcon(null) }) {
                        Text("清除")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Schedule Section Header
            Text(
                text = "打卡计划",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Start Date
            Text(
                text = "开始日期",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (startDate.isNotBlank()) startDate else "选择开始日期 (yyyy-MM-dd)",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // End Date
            Text(
                text = "结束日期",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (endDate.isNotBlank()) endDate else "选择结束日期 (yyyy-MM-dd)",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekday Selector
            Text(
                text = "选择星期",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
                weekdays.forEachIndexed { index, label ->
                    val dayValue = index + 1 // 1=Mon..7=Sun
                    val isSelected = dayValue in selectedWeekdays
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleWeekday(dayValue) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Fields Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "自定义字段",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { viewModel.addField() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加字段",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "定义打卡时需要填写的字段",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            fields.forEachIndexed { index, field ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = field.name,
                        onValueChange = { viewModel.updateFieldName(index, it) },
                        label = { Text("字段名称") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Type selector
                    FilterChip(
                        selected = field.type == "number",
                        onClick = {
                            viewModel.updateFieldType(
                                index,
                                if (field.type == "number") "text" else "number"
                            )
                        },
                        label = {
                            Text(
                                text = if (field.type == "number") "数字" else "文本",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    if (fields.size > 0) {
                        IconButton(onClick = { viewModel.removeField(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除字段",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (fields.isEmpty()) {
                Text(
                    text = "暂无自定义字段",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveProject { onNavigateBack() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = "保存",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        WheelDatePicker(
            initialDate = startDate.ifBlank { LocalDate.now().toString() },
            onDateSelected = { date ->
                viewModel.updateStartDate(date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // End Date Picker Dialog
    if (showIconPicker) {
        ProjectIconPicker(
            selectedIcon = selectedIcon,
            onIconSelected = { viewModel.updateIcon(it) },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showEndDatePicker) {
        WheelDatePicker(
            initialDate = endDate.ifBlank { LocalDate.now().toString() },
            onDateSelected = { date ->
                viewModel.updateEndDate(date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun WheelDatePicker(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = try {
        initialDate.split("-").map { it.toInt() }
    } catch (e: Exception) {
        val now = LocalDate.now()
        listOf(now.year, now.monthValue, now.dayOfMonth)
    }

    var year by remember { mutableStateOf(parts.getOrNull(0)?.toString() ?: LocalDate.now().year.toString()) }
    var month by remember { mutableStateOf(parts.getOrNull(1)?.toString() ?: LocalDate.now().monthValue.toString()) }
    var day by remember { mutableStateOf(parts.getOrNull(2)?.toString() ?: LocalDate.now().dayOfMonth.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = {
            Column {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("年") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("月") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("日") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val dateStr = "%04d-%02d-%02d".format(
                    year.toIntOrNull() ?: LocalDate.now().year,
                    month.toIntOrNull() ?: LocalDate.now().monthValue,
                    day.toIntOrNull() ?: LocalDate.now().dayOfMonth
                )
                onDateSelected(dateStr)
            }) {
                Text("确认")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
