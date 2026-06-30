package com.bulubulu.recordlifeitems.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate

@Composable
fun WheelDatePicker(
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

    var year by remember { mutableStateOf(parts.getOrNull(0) ?: LocalDate.now().year) }
    var month by remember { mutableStateOf(parts.getOrNull(1) ?: LocalDate.now().monthValue) }
    var day by remember { mutableStateOf(parts.getOrNull(2) ?: LocalDate.now().dayOfMonth) }

    var showDirectInput by remember { mutableStateOf(false) }
    var directYear by remember { mutableStateOf(year.toString()) }
    var directMonth by remember { mutableStateOf(month.toString()) }
    var directDay by remember { mutableStateOf(day.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选择日期")
                Text(
                    text = if (showDirectInput) "滚轮" else "手动",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showDirectInput = !showDirectInput
                            if (showDirectInput) {
                                directYear = year.toString()
                                directMonth = month.toString()
                                directDay = day.toString()
                            } else {
                                year = directYear.toIntOrNull() ?: LocalDate.now().year
                                month = directMonth.toIntOrNull() ?: LocalDate.now().monthValue
                                day = directDay.toIntOrNull() ?: LocalDate.now().dayOfMonth
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        },
        text = {
            if (showDirectInput) {
                Column {
                    OutlinedTextField(
                        value = directYear,
                        onValueChange = { directYear = it },
                        label = { Text("年") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = directMonth,
                        onValueChange = { directMonth = it },
                        label = { Text("月") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = directDay,
                        onValueChange = { directDay = it },
                        label = { Text("日") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WheelSelector(
                        items = (1970..2099).toList(),
                        selectedItem = year,
                        onItemSelected = { year = it },
                        label = "年",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    WheelSelector(
                        items = (1..12).toList(),
                        selectedItem = month,
                        onItemSelected = { month = it },
                        label = "月",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val maxDay = try {
                        LocalDate.of(year, month, 1).lengthOfMonth()
                    } catch (e: Exception) {
                        31
                    }
                    WheelSelector(
                        items = (1..maxDay).toList(),
                        selectedItem = if (day > maxDay) maxDay else day,
                        onItemSelected = { day = it },
                        label = "日",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val fy = if (showDirectInput) directYear.toIntOrNull() ?: LocalDate.now().year else year
                val fm = if (showDirectInput) directMonth.toIntOrNull() ?: LocalDate.now().monthValue else month
                val fd = if (showDirectInput) directDay.toIntOrNull() ?: LocalDate.now().dayOfMonth else day
                onDateSelected("%04d-%02d-%02d".format(fy, fm, fd))
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

@Composable
private fun WheelSelector(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val initialIndex = items.indexOf(selectedItem).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val itemHeight = 40.dp

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    onItemSelected(items[index])
                }
            }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .height(120.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(3) { Box(modifier = Modifier.height(itemHeight)) }
                items(items.size) { index ->
                    val item = items[index]
                    val isSelected = item == selectedItem
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%02d".format(item),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (isSelected) 18.sp else 14.sp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                items(3) { Box(modifier = Modifier.height(itemHeight)) }
            }
        }
    }
}
