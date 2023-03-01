package org.simple.clinic.monthlyscreeningreports.form.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.InputFieldComponentData
import org.simple.clinic.questionnaire.component.properties.Integer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    inputFieldComponentData: InputFieldComponentData,
    content: MutableMap<String, Any>
) {
  var text by remember { mutableStateOf(getValue(inputFieldComponentData, content)) }
  TextField(
      value = text,
      onValueChange = { text = it },
      label = { Text(text = inputFieldComponentData.text) },
      singleLine = true,
      shape = RoundedCornerShape(0.dp),
      colors = TextFieldDefaults.textFieldColors(
          containerColor = colorResource(id = R.color.white),
          focusedLabelColor = colorResource(id = R.color.color_on_surface_67),
          unfocusedLabelColor = colorResource(id = R.color.color_on_surface_67),
          focusedIndicatorColor = colorResource(id = R.color.simple_light_blue_500),
          unfocusedIndicatorColor = colorResource(id = R.color.color_on_surface_67),
          cursorColor = colorResource(id = R.color.simple_light_blue_500)
      ),
      keyboardOptions = KeyboardOptions(
          keyboardType = if (inputFieldComponentData.type == Integer)
            KeyboardType.Number else KeyboardType.Text,
          imeAction = ImeAction.Next
      ),
      textStyle = TextStyle(
          fontFamily = FontFamily.SansSerif
      ),
  )
}

private fun getValue(
    inputFieldComponentData: InputFieldComponentData,
    content: Map<String, Any>
): String {
  return if (content.containsKey(inputFieldComponentData.linkId)) {
    when (inputFieldComponentData.type) {
      is Integer -> {
        try {
          (content[inputFieldComponentData.linkId] as Number).toInt().toString()
        } catch (ex: Exception) {
          ""
        }
      }
      else -> content[inputFieldComponentData.linkId].toString()
    }
  } else ""
}
