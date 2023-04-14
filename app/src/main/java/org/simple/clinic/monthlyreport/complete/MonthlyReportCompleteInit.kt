package org.simple.clinic.monthlyreport.complete

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import java.util.UUID

class MonthlyReportCompleteInit(
    private val questionnaireResponseId: UUID
) : Init<MonthlyReportCompleteModel, MonthlyReportCompleteEffect> {
  override fun init(
      model: MonthlyReportCompleteModel
  ): First<MonthlyReportCompleteModel, MonthlyReportCompleteEffect> {
    return first(model,
        LoadQuestionnaireResponseEffect(questionnaireResponseId)
    )
  }
}
