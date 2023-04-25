package org.simple.clinic.monthlyreports.form.compose.util

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.properties.InputFieldType
import org.simple.clinic.questionnaire.component.properties.IntegerType

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun getTextFieldColors(): TextFieldColors {
  return TextFieldDefaults.textFieldColors(
      containerColor = colorResource(id = R.color.white),
      focusedLabelColor = colorResource(id = R.color.color_on_surface_67),
      unfocusedLabelColor = colorResource(id = R.color.color_on_surface_67),
      focusedIndicatorColor = colorResource(id = R.color.simple_light_blue_500),
      unfocusedIndicatorColor = colorResource(id = R.color.color_on_surface_67),
      cursorColor = colorResource(id = R.color.simple_light_blue_500)
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

@Composable
fun getTextFieldMaxLines(inputType: InputFieldType): Int {
  return if (inputType == IntegerType) 1 else 4
}
