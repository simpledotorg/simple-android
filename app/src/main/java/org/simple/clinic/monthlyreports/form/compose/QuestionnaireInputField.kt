package org.simple.clinic.monthlyreports.form.compose

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
import org.simple.clinic.monthlyreports.form.compose.util.getKeyBoardType
import org.simple.clinic.monthlyreports.form.compose.util.textFieldColors
import org.simple.clinic.questionnaire.component.InputFieldComponentData
import org.simple.clinic.questionnaire.component.properties.InputFieldType
import org.simple.clinic.questionnaire.component.properties.IntegerType
import org.simple.clinic.questionnaire.component.properties.StringType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    inputFieldComponentData: InputFieldComponentData,
    content: MutableMap<String, Any?>
) {
  val initValue = getContentValueAsString(inputFieldComponentData, content)
  var text by remember {
    mutableStateOf(TextFieldValue(
        text = initValue,
        selection = TextRange(initValue.length)
    ))
  }
  TextField(
      value = text,
      onValueChange = {
        if (allowUserInput(it.text, inputFieldComponentData.type)) {
          text = it
          setContentValue(it.text, inputFieldComponentData, content)
        }
      },
      label = { Text(text = inputFieldComponentData.text) },
      singleLine = true,
      shape = RoundedCornerShape(0.dp),
      colors = textFieldColors(),
      keyboardOptions = getKeyBoardType(inputType = inputFieldComponentData.type),
      textStyle = TextStyle(
          fontFamily = FontFamily.SansSerif
      ),
  )
}

private fun allowUserInput(text: String, inputType: InputFieldType): Boolean {
  val numericalPattern = Regex("^\\d+\$")

  return when (inputType) {
    is IntegerType -> text.isEmpty() || text.matches(numericalPattern)
    is StringType -> true
    else -> false
  }
}

private fun getContentValueAsString(
    inputFieldComponentData: InputFieldComponentData,
    content: Map<String, Any?>
): String {
  return if (content.containsKey(inputFieldComponentData.linkId)) {
    when (inputFieldComponentData.type) {
      is IntegerType -> {
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
    content: MutableMap<String, Any?>
) {
  when (inputFieldComponentData.type) {
    is IntegerType -> {
      setIntegerContentValue(value, inputFieldComponentData, content)
    }

    else -> content[inputFieldComponentData.linkId] = value
  }
}

private fun setIntegerContentValue(
    value: String,
    inputFieldComponentData: InputFieldComponentData,
    content: MutableMap<String, Any?>
) {
  if (value.isEmpty()) {
    content[inputFieldComponentData.linkId] = null
  } else {
    try {
      content[inputFieldComponentData.linkId] = value.toInt()
    } catch (_: Exception) {
    }
  }
}
