package org.simple.clinic.monthlyscreeningreports.form.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.simple.clinic.monthlyscreeningreports.form.compose.util.getKeyBoardType
import org.simple.clinic.monthlyscreeningreports.form.compose.util.getTextFieldColors
import org.simple.clinic.questionnaire.component.InputFieldComponentData
import org.simple.clinic.questionnaire.component.properties.Integer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    inputFieldComponentData: InputFieldComponentData,
    content: MutableMap<String, Any>
) {
  val initValue = getContentValueAsString(inputFieldComponentData, content)
  var text by remember {
    mutableStateOf(TextFieldValue(
        text = initValue,
        selection = TextRange(initValue.length)
    ))
  }
  val pattern = remember { Regex("^\\d+\$") }
  TextField(
      value = text,
      onValueChange = {
        if (it.text.isEmpty() || it.text.matches(pattern)) {
          text = it
          setContentValue(it.text, inputFieldComponentData, content)
        }
      },
      label = { Text(text = inputFieldComponentData.text) },
      singleLine = true,
      shape = RoundedCornerShape(0.dp),
      colors = getTextFieldColors(),
      keyboardOptions = getKeyBoardType(inputType = inputFieldComponentData.type),
      textStyle = TextStyle(
          fontFamily = FontFamily.SansSerif
      ),
  )
}

private fun getContentValueAsString(
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

private fun setContentValue(
    value: String,
    inputFieldComponentData: InputFieldComponentData,
    content: MutableMap<String, Any>
) {
  when (inputFieldComponentData.type) {
    is Integer -> {
      setIntegerContentValue(value, inputFieldComponentData, content)
    }
    else -> content[inputFieldComponentData.linkId] = value
  }
}

private fun setIntegerContentValue(
    value: String,
    inputFieldComponentData: InputFieldComponentData,
    content: MutableMap<String, Any>
) {
  try {
    content[inputFieldComponentData.linkId] = value.toInt()
  } catch (ex: Exception) {
    if (value.isEmpty()) {
      content.remove(inputFieldComponentData.linkId)
    }
  }
}
