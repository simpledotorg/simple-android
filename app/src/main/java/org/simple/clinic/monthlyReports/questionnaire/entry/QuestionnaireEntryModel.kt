package org.simple.clinic.monthlyReports.questionnaire.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.monthlyReports.questionnaire.Questionnaire

@Parcelize
data class QuestionnaireEntryModel(
    val facility: Facility?,
    val questionnaire: Questionnaire?
) : Parcelable {
  companion object {
    fun default() = QuestionnaireEntryModel(
        facility = null,
        questionnaire = null,
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): QuestionnaireEntryModel {
    return copy(facility = facility)
  }

  fun formLoaded(questionnaire: Questionnaire): QuestionnaireEntryModel {
    return copy(questionnaire = questionnaire)
  }
}
