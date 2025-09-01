package dev.shade.gomauris.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import dev.shade.gomauris.ui.theme.GoMaurisColors
import dev.shade.gomauris.ui.theme.RobotoFontFamily
import dev.shade.gomauris.viewmodel.HomeTabViewModel
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RideChooseTimeScreen(private val screenModal: HomeTabViewModel) : Screen {

    companion object {
        val dateFormat = LocalDate.Format {
            day(padding = Padding.ZERO)
            char('-')
            monthNumber(Padding.ZERO)
            char('-')
            year()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
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
                var showDatePicker by remember { mutableStateOf(false) }
                val selectedDate by screenModal.selectedDate.collectAsState()

                DateButton(
                    text = "Today",
                    isSelected = selectedButton == 0,
                    onClick = {
                        selectedButton = 0
                        screenModal.updateSelectedDate(Clock.System.todayIn(TimeZone.currentSystemDefault()))
                    },
                    modifier = Modifier.weight(1f)
                )

                DateButton(
                    text = "Tomorrow",
                    isSelected = selectedButton == 1,
                    onClick = {
                        selectedButton = 1
                        screenModal.updateSelectedDate(Clock.System.todayIn(TimeZone.currentSystemDefault())
                            .plus(DatePeriod(days = 1)))
                    },
                    modifier = Modifier.weight(1f)
                )

                DateButton(
                    text = selectedDate.format(dateFormat),
                    isSelected = selectedButton == 2,
                    onClick = {
                        selectedButton = 2
                        showDatePicker = true
                    },
                    modifier = Modifier.weight(1f)
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        onDateSelected = { date ->
                            screenModal.updateSelectedDate(date)
                            selectedButton = 2
                            showDatePicker = false
                        },
                        onDismiss = {
                            showDatePicker = false
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                val hours = (0..23).map { it.toString().padStart(2, '0') }
                val minutes = (0..59).map { it.toString().padStart(2, '0') }

                val repeats = 1000
                val startRepeats = repeats / 2
                val initialHourPosition = startRepeats
                val initialMinutePosition = startRepeats

                val hourState =
                    rememberLazyListState(initialFirstVisibleItemIndex = initialHourPosition)
                val minuteState =
                    rememberLazyListState(initialFirstVisibleItemIndex = initialMinutePosition)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    PickerColumn(
                        items = hours,
                        state = hourState,
                        onSelectionChanged = {
                            screenModal.updatetimeHours(it)
                            println(
                                "Selected time: ${screenModal.toLocalTime()}"
                            )
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .wrapContentWidth()
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    PickerColumn(
                        items = minutes,
                        state = minuteState,
                        onSelectionChanged = {
                            screenModal.updatetimeMinute(it)
                            println(
                                "Selected time: ${screenModal.toLocalTime()}"
                            )
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

@Composable
private fun DateButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        shape = RoundedCornerShape(size = 4.dp),
        onClick = onClick,
        modifier = modifier.height(35.dp),
        colors = ButtonColors(
            containerColor = if (isSelected) GoMaurisColors.surfaceBright else GoMaurisColors.surfaceVariant,
            contentColor = if (isSelected) Color.White else Color.Black,
            disabledContentColor = GoMaurisColors.surfaceVariant,
            disabledContainerColor = GoMaurisColors.surfaceVariant,
        ),
        elevation = if (isSelected) {
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        } else {
            ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
        }
    ) {
        Text(
            text = text,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = RobotoFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

    val repeats = 1000
    val infiniteItems = remember(items) {
        List(repeats) { index ->
            items[index % items.size]
        }
    }

    LaunchedEffect(state) {
        state.scroll {
            with(flingBehavior) {
                performFling(10f)
            }
        }
    }

    LaunchedEffect(state, items) {
        snapshotFlow {
            if (state.layoutInfo.viewportSize.height > 0 && state.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                val viewportCenter = state.layoutInfo.viewportSize.height / 2f
                val centerItem = state.layoutInfo.visibleItemsInfo
                    .filter { it.index > 0 }
                    .minByOrNull { itemInfo ->
                        val itemCenter = itemInfo.offset + itemInfo.size / 2f
                        kotlin.math.abs(itemCenter - viewportCenter)
                    }

                centerItem?.let {
                    val actualIndex = (it.index - 1) % items.size
                    items[actualIndex]
                }
            } else null
        }.collect { selectedItem ->
            selectedItem?.let { onSelectionChanged(it) }
        }
    }

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
                    with(LocalDensity.current) {
                        (viewportHeight / 2).toDp() - itemHeight / 2
                    }
                } else {
                    200.dp
                }
                Spacer(Modifier.height(spacerHeight))
            }

            itemsIndexed(infiniteItems) { index, item ->
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
                    color = if (distanceFactor < 0.5f) GoMaurisColors.surfaceBright else color,
                    text = item,
                    fontSize = fontSize,
                    fontWeight = if (distanceFactor < 0.5f) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }

            item {
                val viewportHeight = state.layoutInfo.viewportSize.height
                val spacerHeight = if (viewportHeight > 0) {
                    with(LocalDensity.current) {
                        (viewportHeight / 2).toDp() - itemHeight / 2
                    }
                } else {
                    200.dp
                }
                Spacer(Modifier.height(spacerHeight))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -itemHeight / 1.5f)
                .fillMaxWidth()
                .height(1.dp)
                .background(GoMaurisColors.surfaceBright)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = itemHeight / 1.5f)
                .fillMaxWidth()
                .height(1.dp)
                .background(GoMaurisColors.surfaceBright)
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val selectedDate = Instant.fromEpochMilliseconds(utcTimeMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

                return selectedDate >= today
            }
        }
    )

    DatePickerDialog(
        colors = DatePickerDefaults.colors(
            containerColor = GoMaurisColors.onSurfaceVariant,
        ),
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis?.let { millis ->
                        Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                    }
                    onDateSelected(selectedDate)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoMaurisColors.surfaceBright,
                    contentColor = Color.White
                )
            ) {
                Text("OK", fontFamily = RobotoFontFamily)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = GoMaurisColors.onSurfaceVariant,
                    contentColor = GoMaurisColors.surfaceBright
                )
            ) {
                Text("Cancel", fontFamily = RobotoFontFamily)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color.Transparent,
                titleContentColor = GoMaurisColors.scrim,
                headlineContentColor = GoMaurisColors.scrim,
                weekdayContentColor = GoMaurisColors.scrim,
                subheadContentColor = GoMaurisColors.scrim,
                navigationContentColor = GoMaurisColors.scrim,
                yearContentColor = GoMaurisColors.scrim,
                disabledYearContentColor = GoMaurisColors.outline,
                currentYearContentColor = GoMaurisColors.surfaceBright,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = GoMaurisColors.surfaceBright,
                dayContentColor = GoMaurisColors.scrim,
                disabledDayContentColor = GoMaurisColors.tertiary,
                selectedDayContentColor = Color.White,
                disabledSelectedDayContentColor = GoMaurisColors.outline,
                selectedDayContainerColor = GoMaurisColors.surfaceBright,
                disabledSelectedDayContainerColor = GoMaurisColors.outline,
                todayContentColor = GoMaurisColors.surfaceBright,
                todayDateBorderColor = GoMaurisColors.surfaceBright,
                dayInSelectionRangeContentColor = GoMaurisColors.scrim,
                dayInSelectionRangeContainerColor = GoMaurisColors.surfaceBright.copy(alpha = 0.3f),
                dividerColor = GoMaurisColors.outline.copy(alpha = 0.3f)
            )
        )
    }
}