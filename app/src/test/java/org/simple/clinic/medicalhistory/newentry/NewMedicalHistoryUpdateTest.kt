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
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnDiabetesTreatment
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnHypertensionTreatment
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.sharedTestCode.TestData
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class NewMedicalHistoryUpdateTest {

  private val country = TestData.country(isoCountryCode = Country.INDIA)
  private val defaultModel = NewMedicalHistoryModel.default(country)
  private val facilityWithDiabetesManagementEnabled = TestData.facility(
      uuid = UUID.fromString("3c7bc1c8-1bb6-4c3a-b6d0-52700bdaac5c"),
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false
      )
  )
  private val facilityWithDiabetesManagementDisabled = TestData.facility(
      uuid = UUID.fromString("bbffeac9-296d-4e95-8266-e5c9ac5eedf3"),
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = false,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false
      )
  )
  private val patientEntry = OngoingNewPatientEntry.fromFullName("Anish Acharya")

  private val updateSpec = UpdateSpec(NewMedicalHistoryUpdate())

  @Test
  fun `when the current facility is loaded, update the ui`() {
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
  fun `when diabetes management is enabled and the user clicks save, show the diagnosis required error if hypertension diagnosis is not selected`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, Unanswered)
        .answerChanged(DiagnosedWithDiabetes, No)

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
        .answerChanged(DiagnosedWithHypertension, Yes)
        .answerChanged(DiagnosedWithDiabetes, Unanswered)
        .answerChanged(IsOnHypertensionTreatment(Country.INDIA), No)

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
        .answerChanged(DiagnosedWithHypertension, Unanswered)
        .answerChanged(DiagnosedWithDiabetes, Unanswered)

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
  fun `when diabetes management is disabled and the user clicks save, show diagnosis required error if hypertension diagnosis is not selected`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
        .answerChanged(DiagnosedWithHypertension, Unanswered)
        .answerChanged(DiagnosedWithDiabetes, Unanswered)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowHypertensionDiagnosisRequiredError)
            )
        )
  }

  @Test
  fun `when diabetes management is disabled and the user clicks save, do not show the diagnosis required error if hypertension diagnosis is answered`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
        .answerChanged(DiagnosedWithHypertension, Yes)
        .answerChanged(IsOnHypertensionTreatment(Country.INDIA), No)
        .answerChanged(DiagnosedWithDiabetes, Unanswered)

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
        .answerChanged(DiagnosedWithHypertension, No)
        .answerChanged(DiagnosedWithDiabetes, No)

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
        .answerChanged(DiagnosedWithHypertension, No)
        .answerChanged(DiagnosedWithDiabetes, No)

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
        .answerChanged(DiagnosedWithHypertension, Yes)
        .answerChanged(DiagnosedWithDiabetes, No)
        .answerChanged(IsOnHypertensionTreatment(Country.INDIA), Unanswered)

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
        .answerChanged(DiagnosedWithHypertension, Yes)
        .answerChanged(DiagnosedWithDiabetes, No)
        .answerChanged(IsOnHypertensionTreatment(Country.INDIA), Unanswered)

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

  @Test
  fun `when save is clicked and patient is diagnosed with no hypertension and diabetes, then show change diagnosis dialog`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, No)
        .answerChanged(DiagnosedWithDiabetes, No)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(
            assertThatNext(
                hasModel(model.changeDiagnosisErrorShown()),
                hasEffects(ShowChangeDiagnosisErrorDialog)
            )
        )
  }

  @Test
  fun `when save is clicked and patient is diagnosed with no hypertension and diabetes and change diagnosis error is already shown, then don't show change diagnosis dialog`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, No)
        .answerChanged(DiagnosedWithDiabetes, No)
        .changeDiagnosisErrorShown()

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

  @Test
  fun `when change diagnosis not now is clicked, then register patient`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, No)
        .answerChanged(DiagnosedWithDiabetes, No)

    updateSpec
        .given(model)
        .whenEvent(ChangeDiagnosisNotNowClicked)
        .then(
            assertThatNext(
                hasModel(model.registeringPatient()),
                hasEffects(RegisterPatient(model.ongoingMedicalHistoryEntry))
            )
        )
  }

  @Test
  fun `when save is clicked and patient is diagnosed with diabetes and ongoing diabetes treatment question is not answered and selected country is india, then show error`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, Yes)
        .answerChanged(DiagnosedWithDiabetes, Yes)
        .answerChanged(IsOnHypertensionTreatment(Country.INDIA), Yes)
        .answerChanged(IsOnDiabetesTreatment, Unanswered)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowOngoingDiabetesTreatmentErrorDialog)
        ))
  }

  @Test
  fun `when save is clicked and diabetes management is disable and patient is diagnosed with diabetes and ongoing diabetes treatment question is not answered and selected country is india, then register patient`() {
    val model = defaultModel
        .ongoingPatientEntryLoaded(patientEntry)
        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
        .answerChanged(DiagnosedWithHypertension, Yes)
        .answerChanged(DiagnosedWithDiabetes, Yes)
        .answerChanged(IsOnHypertensionTreatment(Country.INDIA), Yes)
        .answerChanged(IsOnDiabetesTreatment, Unanswered)

    updateSpec
        .given(model)
        .whenEvent(SaveMedicalHistoryClicked())
        .then(assertThatNext(
            hasModel(model.registeringPatient()),
            hasEffects(RegisterPatient(model.ongoingMedicalHistoryEntry))
        ))
  }
}
