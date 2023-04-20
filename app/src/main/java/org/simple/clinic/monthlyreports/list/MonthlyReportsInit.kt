package org.simple.clinic.monthlyreports.list

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.questionnaire.QuestionnaireType

class MonthlyReportsInit(
    val questionnaireType: QuestionnaireType
) : Init<MonthlyReportsModel, MonthlyReportsEffect> {
  override fun init(model: MonthlyReportsModel):
      First<MonthlyReportsModel, MonthlyReportsEffect> {
    return first(model, LoadCurrentFacility, LoadMonthlyReportsEffect(questionnaireType))
  }
}
