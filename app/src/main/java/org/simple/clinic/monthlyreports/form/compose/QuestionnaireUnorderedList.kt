package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.simple.clinic.questionnaire.component.UnorderedListComponentData
import org.simple.clinic.questionnaire.component.UnorderedListItemComponentData

@Composable
fun UnorderedList(
    unorderedListComponentData: UnorderedListComponentData
) {
  Column {
    unorderedListComponentData.children.forEach {
      UnorderedListItem(it)
    }
  }
}
