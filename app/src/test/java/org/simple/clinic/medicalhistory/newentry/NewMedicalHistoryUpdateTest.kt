package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import junitparams.JUnitParamsRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_HYPERTENSION_TREATMENT
import org.simple.clinic.patient.OngoingNewPatientEntry
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class NewMedicalHistoryUpdateTest {

  private val country = TestData.country(isoCountryCode = Country.INDIA)
  private val defaultModel = NewMedicalHistoryModel.default(country)
  private val facilityWithDiabetesManagementEnabled = TestData.facility(
      uuid = UUID.fromString("3c7bc1c8-1bb6-4c3a-b6d0-52700bdaac5c"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
  )
  private val facilityWithDiabetesManagementDisabled = TestData.facility(
      uuid = UUID.fromString("bbffeac9-296d-4e95-8266-e5c9ac5eedf3"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
  )
  private val patientEntry = OngoingNewPatientEntry.fromFullName("Anish Acharya")

  private val updateSpec = UpdateSpec(NewMedicalHistoryUpdate())

  @Test
  fun `when the current facility is loaded with diabetes management enabled, update the ui`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facilityWithDiabetesManagementEnabled))
        .then(
            assertThatNext(
                hasModel(defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the current facility is loaded with diabetes management disabled, update the UI and set the hypertension history answer as YES`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facilityWithDiabetesManagementDisabled))
        .then(
            assertThatNext(
                hasModel(
                    defaultModel
                        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
                        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
                ),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when diabetes management is enabled and the user clicks save, show the diagnosis required error if hypertension diagnosis is not selected`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Unanswered)
        .answerChanged(DIAGNOSED_WITH_DIABETES, No)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowDiagnosisRequiredError)
            )
        )
  }

  @Test
  fun `when diabetes management is enabled and the user clicks save, show the diagnosis required error if diabetes diagnosis is not selected`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
        .answerChanged(DIAGNOSED_WITH_DIABETES, Unanswered)
        .answerChanged(IS_ON_HYPERTENSION_TREATMENT, No)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowDiagnosisRequiredError)
            )
        )
  }

  @Test
  fun `when diabetes management is enabled and the user clicks save, show the diagnosis required error if both diagnosis are not selected`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Unanswered)
        .answerChanged(DIAGNOSED_WITH_DIABETES, Unanswered)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowDiagnosisRequiredError)
            )
        )
  }

  @Test
  fun `when diabetes management is disabled and the user clicks save, do not show the diagnosis required error`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Unanswered)
        .answerChanged(DIAGNOSED_WITH_DIABETES, Unanswered)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasModel(model.registeringPatient()),
                hasEffects(RegisterPatient(model.ongoingMedicalHistoryEntry) as NewMedicalHistoryEffect)
            )
        )
  }

  @Test
  fun `when the patient is registered, then update ui and trigger a sync`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, No)
        .answerChanged(DIAGNOSED_WITH_DIABETES, No)

    val patientUuid = UUID.fromString("c14a06e1-2f60-437e-8845-67f65d4f01a6")

    updateSpec
        .given(model)
        .whenEvent(PatientRegistered(patientUuid))
        .then(
            assertThatNext(
                hasModel(model.patientRegistered()),
                hasEffects(TriggerSync(patientUuid) as NewMedicalHistoryEffect)
            )
        )
  }

  @Test
  fun `when the sync is triggered, open the patient summary sheet`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, No)
        .answerChanged(DIAGNOSED_WITH_DIABETES, No)

    val patientUuid = UUID.fromString("c14a06e1-2f60-437e-8845-67f65d4f01a6")

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(patientUuid))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenPatientSummaryScreen(patientUuid) as NewMedicalHistoryEffect)
            )
        )
  }

  @Test
  fun `when save is clicked and patient is diagnosed with hypertension and ongoing hypertension treatment question is not answered and selected country is india, then show error`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
        .answerChanged(DIAGNOSED_WITH_DIABETES, No)
        .answerChanged(IS_ON_HYPERTENSION_TREATMENT, Unanswered)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowOngoingHypertensionTreatmentError)
            )
        )
  }

  @Test
  fun `when save is clicked and patient is diagnosed with hypertension and ongoing hypertension treatment question is not answered and selected country is not india, then register patient`() {
    val bangladesh = TestData.country(isoCountryCode = Country.BANGLADESH)
    val model = NewMedicalHistoryModel.default(country = bangladesh)
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
        .answerChanged(DIAGNOSED_WITH_DIABETES, No)
        .answerChanged(IS_ON_HYPERTENSION_TREATMENT, Unanswered)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasModel(model.registeringPatient()),
                hasEffects(RegisterPatient(model.ongoingMedicalHistoryEntry))
            )
        )
  }
}
