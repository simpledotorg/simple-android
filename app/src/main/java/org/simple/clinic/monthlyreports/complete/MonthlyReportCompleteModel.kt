package org.simple.clinic.monthlyreports.complete

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class MonthlyReportCompleteModel(
    val questionnaireResponse: QuestionnaireResponse?
) : Parcelable {
  companion object {
    fun default() = MonthlyReportCompleteModel(
        questionnaireResponse = null
    )
  }

  fun questionnaireResponseLoaded(
      questionnaireResponse: QuestionnaireResponse?
  ): MonthlyReportCompleteModel {
    return copy(questionnaireResponse = questionnaireResponse)
  }
}
