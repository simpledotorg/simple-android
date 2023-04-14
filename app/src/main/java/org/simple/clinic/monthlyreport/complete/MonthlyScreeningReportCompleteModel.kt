package org.simple.clinic.monthlyreport.complete

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class MonthlyScreeningReportCompleteModel(
    val questionnaireResponse: QuestionnaireResponse?
) : Parcelable {
  companion object {
    fun default() = MonthlyScreeningReportCompleteModel(
        questionnaireResponse = null
    )
  }

  fun questionnaireResponseLoaded(
      questionnaireResponse: QuestionnaireResponse?
  ): MonthlyScreeningReportCompleteModel {
    return copy(questionnaireResponse = questionnaireResponse)
  }
}
