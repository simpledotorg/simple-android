package org.simple.clinic.monthlyreport.complete

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import java.util.UUID

class MonthlyScreeningReportCompleteInit(
    private val questionnaireResponseId: UUID
) : Init<MonthlyScreeningReportCompleteModel, MonthlyScreeningReportCompleteEffect> {
  override fun init(
      model: MonthlyScreeningReportCompleteModel
  ): First<MonthlyScreeningReportCompleteModel, MonthlyScreeningReportCompleteEffect> {
    return first(model,
        LoadQuestionnaireResponseEffect(questionnaireResponseId)
    )
  }
}
