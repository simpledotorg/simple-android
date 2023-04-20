package org.simple.clinic.monthlyreports.list

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.questionnaire.QuestionnaireType

class MonthlyReportListInit(
    val questionnaireType: QuestionnaireType
) : Init<MonthlyReportListModel, MonthlyReportListEffect> {
  override fun init(model: MonthlyReportListModel):
      First<MonthlyReportListModel, MonthlyReportListEffect> {
    return first(model, LoadCurrentFacility, LoadMonthlyReportListEffect(questionnaireType))
  }
}
