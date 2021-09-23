package org.simple.clinic.medicalhistory.newentry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.OngoingNewPatientEntry

@Parcelize
data class NewMedicalHistoryModel(
    val country: Country,
    val ongoingPatientEntry: OngoingNewPatientEntry?,
    val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry,
    val currentFacility: Facility?,
    val nextButtonState: ButtonState?,
    val hasShownChangeDiagnosisError: Boolean
) : Parcelable {

  val hasLoadedPatientEntry: Boolean
    get() = ongoingPatientEntry != null

  val hasLoadedCurrentFacility: Boolean
    get() = currentFacility != null

  val hasNotInitialized: Boolean
    get() = ongoingPatientEntry == null && currentFacility == null

  val facilityDiabetesManagementEnabled: Boolean
    get() = currentFacility!!.config.diabetesManagementEnabled

  val hasAnsweredBothDiagnosisQuestions: Boolean
    get() = !(ongoingMedicalHistoryEntry.diagnosedWithHypertension == Unanswered || ongoingMedicalHistoryEntry.hasDiabetes == Unanswered)

  val hasAnsweredHypertensionDiagnosis: Boolean
    get() = ongoingMedicalHistoryEntry.diagnosedWithHypertension != Unanswered

  val registeringPatient: Boolean
    get() = nextButtonState == ButtonState.SAVING

  val diagnosedWithHypertension: Boolean
    get() = ongoingMedicalHistoryEntry.diagnosedWithHypertension == Yes

  val diagnosedWithDiabetes: Boolean
    get() = ongoingMedicalHistoryEntry.hasDiabetes == Yes

  val answeredIsOnHypertensionTreatment: Boolean
    get() = ongoingMedicalHistoryEntry.isOnHypertensionTreatment != Unanswered

  val answeredIsOnDiabetesTreatment: Boolean
    get() = ongoingMedicalHistoryEntry.isOnDiabetesTreatment != Unanswered

  val showOngoingHypertensionTreatment: Boolean
    get() = diagnosedWithHypertension && country.isoCountryCode == Country.INDIA

  val showOngoingDiabetesTreatment: Boolean
    get() = diagnosedWithDiabetes && country.isoCountryCode == Country.INDIA

  private val hasNoHypertension: Boolean
    get() = ongoingMedicalHistoryEntry.diagnosedWithHypertension == No

  private val hasNoDiabetes: Boolean
    get() = ongoingMedicalHistoryEntry.hasDiabetes == No

  val showChangeDiagnosisError: Boolean
    get() = facilityDiabetesManagementEnabled && !hasShownChangeDiagnosisError && hasNoHypertension && hasNoDiabetes

  companion object {
    fun default(country: Country): NewMedicalHistoryModel = NewMedicalHistoryModel(
        country = country,
        ongoingPatientEntry = null,
        ongoingMedicalHistoryEntry = OngoingMedicalHistoryEntry(),
        currentFacility = null,
        nextButtonState = null,
        hasShownChangeDiagnosisError = false
    )
  }

  fun answerChanged(question: MedicalHistoryQuestion, answer: Answer): NewMedicalHistoryModel {
    return copy(ongoingMedicalHistoryEntry = ongoingMedicalHistoryEntry.answerChanged(question, answer))
  }

  fun ongoingPatientEntryLoaded(entry: OngoingNewPatientEntry): NewMedicalHistoryModel {
    return copy(ongoingPatientEntry = entry)
  }

  fun currentFacilityLoaded(facility: Facility): NewMedicalHistoryModel {
    return copy(currentFacility = facility)
  }

  fun registeringPatient(): NewMedicalHistoryModel {
    return copy(nextButtonState = ButtonState.SAVING)
  }

  fun patientRegistered(): NewMedicalHistoryModel {
    return copy(nextButtonState = ButtonState.SAVED)
  }

  fun changeDiagnosisErrorShown(): NewMedicalHistoryModel {
    return copy(hasShownChangeDiagnosisError = true)
  }
}
