package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import org.simple.clinic.R
import org.simple.clinic.monthlyreports.form.compose.util.keyboardType
import org.simple.clinic.monthlyreports.form.compose.util.textFieldColors
import org.simple.clinic.questionnaire.component.InputFieldComponentData
import org.simple.clinic.questionnaire.component.properties.InputFieldViewType
import org.simple.clinic.widgets.montyeardatepicker.MonthYearPickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateInputField(
    inputFieldComponentData: InputFieldComponentData,
    content: MutableMap<String, Any?>,
    modifier: Modifier = Modifier,
) {
  val dateFormatter = remember {
    DateTimeFormatter.ofPattern(inputFieldComponentData.viewFormat ?: "dd-MM-yyyy")
  }
  var selectedDate by remember {
    mutableStateOf<LocalDate?>(null)
  }
  var showDatePickerDialog by remember {
    mutableStateOf(false)
  }
  val selectedDateFormattedString = remember(selectedDate) {
    selectedDate?.let {
      dateFormatter.format(selectedDate)
    }.orEmpty()
  }
  val interactionSource = remember { MutableInteractionSource() }

  LaunchedEffect(Unit) {
    interactionSource.interactions.collectLatest { interaction ->
      when (interaction) {
        is PressInteraction.Release -> {
          showDatePickerDialog = true
        }
      }
    }
  }

  // Issue: https://issuetracker.google.com/issues/289237728
  CompositionLocalProvider(LocalTextInputService provides null) {
    TextField(
        modifier = Modifier
            .clickable { showDatePickerDialog = true }
            .then(modifier),
        value = selectedDateFormattedString,
        onValueChange = { },
        label = { Text(text = inputFieldComponentData.text) },
        readOnly = true,
        singleLine = true,
        shape = RoundedCornerShape(0.dp),
        colors = textFieldColors(
            focusedIndicatorColor = colorResource(id = R.color.color_on_surface_67),
            unfocusedIndicatorColor = colorResource(id = R.color.color_on_surface_67)
        ),
        keyboardOptions = keyboardType(inputType = inputFieldComponentData.type),
        textStyle = TextStyle(
            fontFamily = FontFamily.SansSerif
        ),
        interactionSource = interactionSource
    )
  }

  if (showDatePickerDialog) {
    when (inputFieldComponentData.viewType) {
      InputFieldViewType.MonthYearPicker -> {
        MonthYearPickerDialog(
            selectedDate = selectedDate,
            daysRange = IntRange(
                start = inputFieldComponentData.validations.allowedDaysInPast ?: TEN_YEARS_IN_DAYS,
                endInclusive = inputFieldComponentData.validations.allowedDaysInFuture ?: TEN_YEARS_IN_DAYS
            ),
            onDismissRequest = { showDatePickerDialog = false }
        ) {
          selectedDate = it
          content[inputFieldComponentData.linkId] = it.toString()
        }
      }

      InputFieldViewType.InputField, is InputFieldViewType.UnknownType, null -> {
        /* no-op */
      }
    }
  }
}

private const val TEN_YEARS_IN_DAYS = 3650
