package org.simple.clinic.widgets.montyeardatepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.monthlyreports.form.compose.util.textFieldColors
import org.simple.clinic.util.RealUserClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.montyeardatepicker.MonthYearPickerValue.Month
import org.simple.clinic.widgets.montyeardatepicker.MonthYearPickerValue.Year
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.time.Month as JavaMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerDialog(
    onDismissRequest: () -> Unit,
    daysRange: IntRange,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit
) {
  val dateFormatter = remember {
    DateTimeFormatter.ofPattern("MM/yyyy")
  }
  val monthYearPickerState = rememberMonthYearPickerState(
      initialSelectedDate = selectedDate,
      daysRange = daysRange
  )

  AlertDialog(
      modifier = modifier,
      onDismissRequest = onDismissRequest
  ) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(colorResource(id = R.color.white))
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 24.dp,
                bottom = 8.dp
            )
    ) {
      MonthYearPickerContent(monthYearPickerState)

      if (!monthYearPickerState.isSelectedDateValid) {
        Spacer(modifier = Modifier.requiredHeight(8.dp))

        Text(
            text = stringResource(
                id = R.string.month_date_picker_invalid_date,
                dateFormatter.format(monthYearPickerState.minDate),
                dateFormatter.format(monthYearPickerState.maxDate)
            ),
            color = colorResource(id = R.color.simple_red_500)
        )
      }

      Spacer(modifier = Modifier.requiredHeight(16.dp))

      Row(
          modifier = Modifier.align(Alignment.End)
      ) {
        TextButton(onClick = onDismissRequest) {
          Text(
              text = stringResource(id = android.R.string.cancel),
              color = colorResource(id = R.color.simple_light_blue_500)
          )
        }

        TextButton(
            onClick = {
              onDateSelected(monthYearPickerState.selectedDate)
              onDismissRequest()
            },
            enabled = monthYearPickerState.isSelectedDateValid
        ) {
          Text(
              text = stringResource(id = android.R.string.ok),
              color = if (monthYearPickerState.isSelectedDateValid) {
                colorResource(id = R.color.simple_light_blue_500)
              } else {
                colorResource(id = R.color.color_on_surface_34)
              }
          )
        }
      }
    }
  }
}

@Composable
fun MonthYearPickerContent(
    state: MonthYearPickerState
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    DropdownInputField(
        modifier = Modifier.weight(1f),
        selectedItem = state.selectedMonth,
        items = state.months,
        transformLabel = {
          "${it.toString().padStart(2, '0')} - " + JavaMonth.of(it).getDisplayName(TextStyle.SHORT, Locale.US)
        },
        onItemSelected = {
          state.updateSelectedMonth(it)
        }
    )

    DropdownInputField(
        modifier = Modifier.weight(1f),
        selectedItem = state.selectedYear,
        items = state.years.map(::Year),
        onItemSelected = {
          state.updateSelectedYear(it)
        }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : MonthYearPickerValue> DropdownInputField(
    selectedItem: T,
    items: List<T>,
    modifier: Modifier = Modifier,
    transformLabel: (Int) -> String = { it.toString() },
    onItemSelected: (T) -> Unit
) {
  var isDropdownExpanded by remember {
    mutableStateOf(false)
  }

  ExposedDropdownMenuBox(
      modifier = modifier,
      expanded = isDropdownExpanded,
      onExpandedChange = {
        isDropdownExpanded = it
      }
  ) {
    // Issue: https://issuetracker.google.com/issues/289237728
    CompositionLocalProvider(LocalTextInputService provides null) {
      TextField(
          modifier = Modifier.menuAnchor(),
          value = transformLabel(selectedItem.value),
          onValueChange = {},
          shape = RoundedCornerShape(0.dp),
          readOnly = true,
          singleLine = true,
          colors = textFieldColors(
              focusedIndicatorColor = colorResource(id = R.color.color_on_surface_67),
              unfocusedIndicatorColor = colorResource(id = R.color.color_on_surface_67)
          ),
          trailingIcon = {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
          }
      )
    }

    ExposedDropdownMenu(
        modifier = Modifier
            .background(Color.White)
            .requiredHeight(250.dp),
        expanded = isDropdownExpanded,
        onDismissRequest = {
          isDropdownExpanded = false
        }
    ) {
      items.forEach { item ->
        DropdownMenuItem(
            text = {
              Text(text = transformLabel(item.value))
            },
            onClick = {
              onItemSelected(item)
              isDropdownExpanded = false
            }
        )
      }
    }
  }
}

@Composable
fun rememberMonthYearPickerState(initialSelectedDate: LocalDate?, daysRange: IntRange): MonthYearPickerState {
  val userClock = LocalUserClock.current

  return remember {
    MonthYearPickerState(
        userClock = userClock,
        initialSelectedDate = initialSelectedDate,
        daysRange = daysRange
    )
  }
}

@Stable
class MonthYearPickerState(
    userClock: UserClock,
    initialSelectedDate: LocalDate?,
    daysRange: IntRange,
) {
  private val currentDate = LocalDate.now(userClock)
  val minDate = currentDate.minusDays(daysRange.first.toLong())
  val maxDate = currentDate.plusDays(daysRange.last.toLong())

  val years = minDate.year..maxDate.year
  val months = JavaMonth.values().map { Month(it.value) }

  var selectedYear by mutableStateOf(Year(initialSelectedDate?.year ?: currentDate.year))
    private set

  var selectedMonth by mutableStateOf(Month(initialSelectedDate?.monthValue ?: currentDate.monthValue))
    private set

  var isSelectedDateValid by mutableStateOf(true)
    private set

  val selectedDate: LocalDate
    get() {
      val yearMonth = YearMonth.of(selectedYear.value, selectedMonth.value)
      return yearMonth.atEndOfMonth()
    }

  init {
    updateSelectedYear(Year(selectedYear.value))
  }

  fun updateSelectedMonth(selectedMonth: Month) {
    this.selectedMonth = selectedMonth
    validateSelectedDate()
  }

  fun updateSelectedYear(selectedYear: Year) {
    this.selectedYear = selectedYear
    validateSelectedDate()
  }

  private fun validateSelectedDate() {
    isSelectedDateValid = selectedDate in minDate..maxDate
  }
}

sealed interface MonthYearPickerValue {

  val value: Int

  @JvmInline
  value class Month(override val value: Int) : MonthYearPickerValue

  @JvmInline
  value class Year(override val value: Int) : MonthYearPickerValue
}

val LocalUserClock = staticCompositionLocalOf<UserClock> { RealUserClock(ZoneId.systemDefault()) }
