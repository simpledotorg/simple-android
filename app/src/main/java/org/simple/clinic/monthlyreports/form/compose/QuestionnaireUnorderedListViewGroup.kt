package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.simple.clinic.questionnaire.component.UnorderedListViewGroupComponentData

@Composable
fun UnorderedListViewGroup(
    unorderedListViewGroupComponentData: UnorderedListViewGroupComponentData
) {
  Column {
    unorderedListViewGroupComponentData.children.forEach {
      UnorderedListItem(it)
    }
  }
}
