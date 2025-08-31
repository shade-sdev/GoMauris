package dev.shade.gomauris.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import cafe.adriel.voyager.core.screen.Screen
import dev.shade.gomauris.ui.theme.GoMaurisColors
import dev.shade.gomauris.ui.theme.RobotoFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import org.jetbrains.compose.ui.tooling.preview.Preview

class RideChooseTimeScreen() : Screen {

    @Composable
    @Preview
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = "Schedule a Ride",
                        color = Color.Black,
                        fontFamily = RobotoFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = GoMaurisColors.outline,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.wrapContentHeight()
                    .padding(PaddingValues(top = 15.dp)),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                var selectedButton by remember { mutableStateOf(0) }

                Button(
                    shape = RoundedCornerShape(size = 4.dp),
                    onClick = { selectedButton = 0 },
                    modifier = Modifier.weight(1f)
                        .height(35.dp),
                    colors = ButtonColors(
                        containerColor = if (selectedButton == 0) GoMaurisColors.surfaceBright else GoMaurisColors.surfaceVariant,
                        contentColor = if (selectedButton == 0) Color.White else Color.Black,
                        disabledContentColor = GoMaurisColors.surfaceVariant,
                        disabledContainerColor = GoMaurisColors.surfaceVariant,
                    )
                )
                {
                    Text(
                        text = "Today",
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = RobotoFontFamily
                    )
                }
                Button(
                    shape = RoundedCornerShape(size = 4.dp),
                    onClick = { selectedButton = 1 },
                    modifier = Modifier.weight(1f).height(35.dp),
                    colors = ButtonColors(
                        containerColor = if (selectedButton == 1) GoMaurisColors.surfaceBright else GoMaurisColors.surfaceVariant,
                        contentColor = if (selectedButton == 1) Color.White else Color.Black,
                        disabledContentColor = GoMaurisColors.surfaceVariant,
                        disabledContainerColor = GoMaurisColors.surfaceVariant,
                    )
                )
                {
                    Text(
                        text = "Tomorrow",
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = RobotoFontFamily
                    )
                }
                Button(
                    shape = RoundedCornerShape(size = 4.dp),
                    onClick = { selectedButton = 2 },
                    modifier = Modifier.weight(1f).height(35.dp),
                    colors = ButtonColors(
                        containerColor = if (selectedButton == 2) GoMaurisColors.surfaceBright else GoMaurisColors.surfaceVariant,
                        contentColor = if (selectedButton == 2) Color.White else Color.Black,
                        disabledContentColor = GoMaurisColors.surfaceVariant,
                        disabledContainerColor = GoMaurisColors.surfaceVariant,
                    )
                )
                {
                    Text(
                        text = "Select Date",
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = RobotoFontFamily
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f)
                    .fillMaxSize()
            ) {
                val hours = (0..23).map { it.toString().padStart(2, '0') }
                val minutes = (0..59).map { it.toString().padStart(2, '0') }

                val hourState = rememberLazyListState(initialFirstVisibleItemIndex = 11)
                val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = 30)

                var selectedHour by remember { mutableStateOf("12") }
                var selectedMinute by remember { mutableStateOf("30") }

                Column(
                    modifier = Modifier.weight(1f)
                        .fillMaxSize()
                ) {
                    PickerColumn(
                        items = hours,
                        state = hourState,
                        onSelectionChanged = {
                            selectedHour = it
                            println(selectedHour)
                        }
                    )
                }

                Column(
                    modifier = Modifier.wrapContentWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        color = GoMaurisColors.surfaceBright,
                        text = ":",
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        fontFamily = RobotoFontFamily
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                        .fillMaxSize()
                ) {
                    PickerColumn(
                        items = minutes,
                        state = minuteState,
                        onSelectionChanged = {
                            selectedMinute = it
                            println(selectedMinute)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.wrapContentHeight(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Row {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = GoMaurisColors.outline,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            10.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonColors(
                                containerColor = GoMaurisColors.onSurfaceVariant,
                                contentColor = GoMaurisColors.surfaceBright,
                                disabledContentColor = GoMaurisColors.surfaceTint,
                                disabledContainerColor = GoMaurisColors.surfaceTint,
                            )
                        )
                        {
                            Text(
                                text = "Cancel",
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                fontFamily = RobotoFontFamily
                            )
                        }

                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            colors = ButtonColors(
                                containerColor = GoMaurisColors.surfaceBright,
                                contentColor = Color.White,
                                disabledContentColor = GoMaurisColors.surfaceBright,
                                disabledContainerColor = GoMaurisColors.surfaceBright,
                            )
                        )
                        {
                            Text(
                                text = "Next",
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = RobotoFontFamily
                            )
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked() {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis.toString()

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            label = { Text("DOB") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }
        }
    }
}


@Composable
fun PickerColumn(
    items: List<String>,
    state: LazyListState,
    onSelectionChanged: (String) -> Unit = {}
) {
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val itemHeight = 50.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    var hasPerformedInitialSnap by remember { mutableStateOf(false) }

    // Track the currently selected item
    LaunchedEffect(state) {
        snapshotFlow {
            if (state.layoutInfo.viewportSize.height > 0 && state.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                val viewportCenter = state.layoutInfo.viewportSize.height / 2f

                // Find the item closest to center (excluding spacer at index 0)
                val centerItem = state.layoutInfo.visibleItemsInfo
                    .filter { it.index > 0 }
                    .minByOrNull { itemInfo ->
                        val itemCenter = itemInfo.offset + itemInfo.size / 2f
                        kotlin.math.abs(itemCenter - viewportCenter)
                    }

                centerItem?.let {
                    val actualIndex = it.index - 1 // -1 because spacer is at index 0
                    if (actualIndex in items.indices) {
                        items[actualIndex]
                    } else null
                }
            } else null
        }.collect { selectedItem ->
            selectedItem?.let { onSelectionChanged(it) }
        }
    }

    LaunchedEffect(state) {
        snapshotFlow {
            state.layoutInfo.totalItemsCount > 0 &&
                    state.layoutInfo.viewportSize.height > 0 &&
                    state.layoutInfo.visibleItemsInfo.isNotEmpty()
        }
            .filter { it && !hasPerformedInitialSnap }
            .take(1)
            .collect {
                delay(100)

                val viewportCenter = state.layoutInfo.viewportSize.height / 2f
                val targetItemIndex = state.firstVisibleItemIndex + 1
                val targetItem =
                    state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetItemIndex }

                targetItem?.let { item ->
                    val itemCenter = item.offset + item.size / 2f
                    val adjustment = viewportCenter - itemCenter - 25f
                    state.animateScrollBy(adjustment)
                }

                hasPerformedInitialSnap = true
            }
    }

    // ... rest of your Box and LazyColumn code stays the same
    Box(modifier = Modifier.fillMaxHeight()) {
        LazyColumn(
            state = state,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                val viewportHeight = state.layoutInfo.viewportSize.height
                val spacerHeight = if (viewportHeight > 0) {
                    with(LocalDensity.current) { (viewportHeight / 2).toDp() - itemHeight / 2 }
                } else {
                    200.dp
                }
                Spacer(Modifier.height(spacerHeight))
            }

            itemsIndexed(items) { index, item ->
                val viewportHeight = state.layoutInfo.viewportSize.height
                val viewportCenter = viewportHeight / 2f
                val itemInfo =
                    state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index + 1 }

                val distanceFactor = if (itemInfo != null && viewportHeight > 0) {
                    val itemCenter = itemInfo.offset + itemInfo.size / 2f
                    val distanceFromCenter = kotlin.math.abs(itemCenter - viewportCenter)
                    distanceFromCenter / itemHeightPx
                } else {
                    Float.MAX_VALUE
                }

                val fontSize = lerp(32.sp, 20.sp, distanceFactor.coerceIn(0f, 1f))
                val color = Color.Black.copy(alpha = (1f - distanceFactor).coerceIn(0.3f, 1f))

                Text(
                    text = item,
                    fontSize = fontSize,
                    fontWeight = if (distanceFactor < 0.5f) FontWeight.Bold else FontWeight.Medium,
                    color = color,
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }

            item {
                val viewportHeight = state.layoutInfo.viewportSize.height
                val spacerHeight = if (viewportHeight > 0) {
                    with(LocalDensity.current) { (viewportHeight / 2).toDp() - itemHeight / 2 }
                } else {
                    200.dp
                }
                Spacer(Modifier.height(spacerHeight))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -itemHeight / 2)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = itemHeight / 2)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )
    }
}

// Method 2: Create a helper function to get selected value from state
@Composable
fun getSelectedValue(items: List<String>, state: LazyListState): String? {
    return remember(state.layoutInfo) {
        if (state.layoutInfo.viewportSize.height > 0 && state.layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val viewportCenter = state.layoutInfo.viewportSize.height / 2f

            val centerItem = state.layoutInfo.visibleItemsInfo
                .filter { it.index > 0 } // Exclude spacer
                .minByOrNull { itemInfo ->
                    val itemCenter = itemInfo.offset + itemInfo.size / 2f
                    kotlin.math.abs(itemCenter - viewportCenter)
                }

            centerItem?.let {
                val actualIndex = it.index - 1 // -1 because spacer is at index 0
                if (actualIndex in items.indices) {
                    items[actualIndex]
                } else null
            }
        } else null
    }
}