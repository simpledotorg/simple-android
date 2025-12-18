package org.simple.clinic.summary.medicalhistory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.summary.PatientSummaryChildModel
import java.util.UUID

@Parcelize
data class MedicalHistorySummaryModel(
    val patientUuid: UUID,
    val medicalHistory: MedicalHistory? = null,
    val currentFacility: Facility? = null,
    val showIsSmokingQuestion: Boolean,
    val showSmokelessTobaccoQuestion: Boolean,
    val showHypertensionSuspectedOption: Boolean,
    val showDiabetesSuspectedOption: Boolean,
) : Parcelable, PatientSummaryChildModel {

  companion object {
    fun create(
        patientUuid: UUID,
        showIsSmokingQuestion: Boolean,
        showSmokelessTobaccoQuestion: Boolean,
    ): MedicalHistorySummaryModel = MedicalHistorySummaryModel(
        patientUuid = patientUuid,
        showIsSmokingQuestion = showIsSmokingQuestion,
        showSmokelessTobaccoQuestion = showSmokelessTobaccoQuestion,
        showHypertensionSuspectedOption = false,
        showDiabetesSuspectedOption = false,
    )
  }

  val hasLoadedMedicalHistory: Boolean
    get() = medicalHistory != null

  val hasLoadedCurrentFacility: Boolean
    get() = currentFacility != null

  override fun readyToRender(): Boolean {
    return hasLoadedMedicalHistory && hasLoadedCurrentFacility
  }

  fun medicalHistoryLoaded(medicalHistory: MedicalHistory): MedicalHistorySummaryModel {
    return copy(medicalHistory = medicalHistory)
  }

  fun currentFacilityLoaded(facility: Facility): MedicalHistorySummaryModel {
    return copy(currentFacility = facility)
  }

  fun answerToggled(question: MedicalHistoryQuestion, answer: Answer): MedicalHistorySummaryModel {
    return copy(medicalHistory = medicalHistory!!.answered(question, answer))
  }

  fun diagnosisSuspectedOptionVisibilityLoaded(
      showHypertensionSuspectedOption: Boolean,
      showDiabetesSuspectedOption: Boolean
  ): MedicalHistorySummaryModel {
    return copy(showHypertensionSuspectedOption = showHypertensionSuspectedOption, showDiabetesSuspectedOption = showDiabetesSuspectedOption)
  }
}
