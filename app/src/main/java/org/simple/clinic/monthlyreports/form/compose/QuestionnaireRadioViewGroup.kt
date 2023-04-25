package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.simple.clinic.questionnaire.component.RadioViewGroupComponentData

@Composable
fun RadioViewGroup(
    radioViewGroupComponentData: RadioViewGroupComponentData,
    content: MutableMap<String, Any?>
) {
  val selectedValue = remember { mutableStateOf(content[radioViewGroupComponentData.linkId]) }

  val isSelectedItem: (String) -> Boolean = { selectedValue.value == it }
  val onChangeState: (String) -> Unit = {
    selectedValue.value = it
    content[radioViewGroupComponentData.linkId] = it
  }

  Row {
    radioViewGroupComponentData.children.forEach {
      QuestionnaireRadioButton(
          isSelected = isSelectedItem(it.text),
          onClick = { selectedButtonText ->
            onChangeState(selectedButtonText)
          },
          it.text
      )
    }
  }
}
