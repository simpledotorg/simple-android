package org.simple.clinic.monthlyreports.form.compose.util

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.properties.InputFieldType
import org.simple.clinic.questionnaire.component.properties.IntegerType

@Composable
fun textFieldColors(
    containerColor: Color = colorResource(id = R.color.white),
    focusedLabelColor: Color = colorResource(id = R.color.color_on_surface_67),
    unfocusedLabelColor: Color = colorResource(id = R.color.color_on_surface_67),
    focusedIndicatorColor: Color = colorResource(id = R.color.simple_light_blue_500),
    unfocusedIndicatorColor: Color = colorResource(id = R.color.color_on_surface_67),
    cursorColor: Color = colorResource(id = R.color.simple_light_blue_500)
): TextFieldColors {
  return TextFieldDefaults.colors(
      focusedContainerColor = containerColor,
      unfocusedContainerColor = containerColor,
      disabledContainerColor = containerColor,
      cursorColor = cursorColor,
      focusedIndicatorColor = focusedIndicatorColor,
      unfocusedIndicatorColor = unfocusedIndicatorColor,
      focusedLabelColor = focusedLabelColor,
      unfocusedLabelColor = unfocusedLabelColor,
  )
}

@Composable
fun getKeyBoardType(inputType: InputFieldType): KeyboardOptions {
  return KeyboardOptions(
      keyboardType = if (inputType == IntegerType)
        KeyboardType.Number else KeyboardType.Text,
      imeAction = ImeAction.Next
  )
}
