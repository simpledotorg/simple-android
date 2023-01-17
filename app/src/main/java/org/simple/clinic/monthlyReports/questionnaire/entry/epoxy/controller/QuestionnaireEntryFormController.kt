package org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.controller

import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.group
import org.simple.clinic.R
import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.HeaderComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.InputFieldComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.LineSeparatorComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.SeparatorComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.SubHeaderComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.ViewGroupComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.properties.Horizontal
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.header
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.inputField
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.lineSeparator
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.separator
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.subHeader
import java.util.UUID

class QuestionnaireEntryFormController : EpoxyController() {

  var questionnaireFormLayout: BaseComponentData? = null
    set(value) {
      field = value
      requestModelBuild()
    }

  override fun buildModels() {
    getComponent(questionnaireFormLayout)
  }

  private fun getComponent(baseComponentData: BaseComponentData?) {
    when (baseComponentData) {
      is ViewGroupComponentData -> getViewGroupComponent(baseComponentData)
      is HeaderComponentData -> getHeaderComponent(baseComponentData)
      is SubHeaderComponentData -> getSubHeaderComponent(baseComponentData)
      is SeparatorComponentData -> getSeparatorComponent(baseComponentData)
      is LineSeparatorComponentData -> getLineSeparatorComponent(baseComponentData)
      is InputFieldComponentData -> getInputFieldSeparatorComponent(baseComponentData)
    }
  }

  private fun getViewGroupComponent(viewGroupComponentData: ViewGroupComponentData) {
    val layout = if (viewGroupComponentData.displayProperties.orientation == Horizontal)
      R.layout.view_questionnaire_horizontal_view_group
    else R.layout.view_questionnaire_vertical_view_group

    group {
      id("dsada")
      layout(layout)
      viewGroupComponentData.children?.forEach {
        this@QuestionnaireEntryFormController.getComponent(it)
      }
    }
  }

  private fun getHeaderComponent(headerComponentData: HeaderComponentData) {
    header {
      id(UUID.fromString("f65762d2-1b7e-4142-befd-1dd91614cd90").toString())
      title(headerComponentData.text)
    }
  }

  private fun getSubHeaderComponent(subHeaderComponentData: SubHeaderComponentData) {
    subHeader {
      id(UUID.fromString("f65762d2-1b7e-4142-befd-1dd91614cd90").toString())
      title(subHeaderComponentData.text)
    }
  }

  private fun getSeparatorComponent(separatorComponentData: SeparatorComponentData) {
    separator {
      id(UUID.fromString("f65762d2-1b7e-4142-befd-1dd91614cd90").toString())
    }
  }

  private fun getLineSeparatorComponent(lineSeparatorComponentData: LineSeparatorComponentData) {
    lineSeparator {
      id(UUID.fromString("f65762d2-1b7e-4142-befd-1dd91614cd90").toString())
    }
  }

  private fun getInputFieldSeparatorComponent(inputFieldComponentData: InputFieldComponentData) {
    inputField {
      id(UUID.fromString("f65762d2-1b7e-4142-befd-1dd91614cd90").toString())
    }
  }
}
