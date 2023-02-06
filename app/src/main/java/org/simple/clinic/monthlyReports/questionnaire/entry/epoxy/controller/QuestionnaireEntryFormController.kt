package org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.controller

import com.airbnb.epoxy.TypedEpoxyController
import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.HeaderComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.InputViewGroupComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.LineSeparatorComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.SeparatorComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.SubHeaderComponentData
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.header
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.inputGroup
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.lineSeparator
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.separator
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model.subHeader

class QuestionnaireEntryFormController : TypedEpoxyController<List<BaseComponentData>>() {

  override fun buildModels(data: List<BaseComponentData>) {
    data.forEach {
      when (it) {
        is HeaderComponentData -> getHeaderComponent(it)
        is SubHeaderComponentData -> getSubHeaderComponent(it)
        is InputViewGroupComponentData -> getInputViewGroupComponent(it)
        is SeparatorComponentData -> getSeparatorComponent(it)
        is LineSeparatorComponentData -> getLineSeparatorComponent(it)
      }
    }
  }

  private fun getInputViewGroupComponent(inputViewGroupComponentData: InputViewGroupComponentData) {
    inputGroup {
      id(inputViewGroupComponentData.id)
      inputViewGroupComponentData(inputViewGroupComponentData)
    }
  }

  private fun getHeaderComponent(headerComponentData: HeaderComponentData) {
    header {
      id(headerComponentData.id)
      title(headerComponentData.text)
    }
  }

  private fun getSubHeaderComponent(subHeaderComponentData: SubHeaderComponentData) {
    subHeader {
      id(subHeaderComponentData.id)
      title(subHeaderComponentData.text)
    }
  }

  private fun getSeparatorComponent(separatorComponentData: SeparatorComponentData) {
    separator {
      id(separatorComponentData.id)
    }
  }

  private fun getLineSeparatorComponent(lineSeparatorComponentData: LineSeparatorComponentData) {
    lineSeparator {
      id(lineSeparatorComponentData.id)
    }
  }
}
