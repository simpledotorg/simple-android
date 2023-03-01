package org.simple.clinic.monthlyscreeningreports.form.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.HeaderComponentData
import org.simple.clinic.questionnaire.component.InputViewGroupComponentData
import org.simple.clinic.questionnaire.component.LineSeparatorComponentData
import org.simple.clinic.questionnaire.component.SeparatorComponentData
import org.simple.clinic.questionnaire.component.SubHeaderComponentData
import org.simple.clinic.questionnaire.component.ViewGroupComponentData

@Composable
fun QuestionnaireFormContainer(
    viewGroupComponentData: ViewGroupComponentData,
    content: MutableMap<String, Any>
) {
  Column(
      modifier = Modifier
          .verticalScroll(state = ScrollState(0))
          .padding(horizontal = dimensionResource(id = R.dimen.spacing_16))
  ) {
    viewGroupComponentData.children?.forEach {
      when (it) {
        is HeaderComponentData -> {
          Header(it)
        }
        is SubHeaderComponentData -> {
          SubHeader(it)
        }
        is InputViewGroupComponentData -> {
          InputGroup(it, content)
        }
        is SeparatorComponentData -> {
          Separator()
        }
        is LineSeparatorComponentData -> {
          LineSeparator()
        }
      }
    }
  }
}
