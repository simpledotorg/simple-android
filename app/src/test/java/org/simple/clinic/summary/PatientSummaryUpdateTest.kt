package org.simple.clinic.summary

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.DONE_CLICK
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import java.util.UUID

class PatientSummaryUpdateTest {

  private val patientUuid = UUID.fromString("93a131b0-890e-41a3-88ec-b35b48efc6c5")
  private val defaultModel = PatientSummaryModel.from(ViewExistingPatient, patientUuid)

  private val patient = TestData.patient(patientUuid)
  private val patientAddress = TestData.patientAddress(patient.addressUuid)
  private val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
  private val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", BpPassport))
  private val bangladeshNationalId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", BangladeshNationalId))
  private val facility = TestData.facility(uuid = UUID.fromString("f7666e67-2c8b-4ef4-9e6d-72bfcbb65db0"))

  private val patientSummaryProfile = PatientSummaryProfile(
      patient = patient,
      address = patientAddress,
      phoneNumber = phoneNumber,
      bpPassport = bpPassport,
      alternativeId = bangladeshNationalId,
      facility = facility
  )

  private val facilityWithDiabetesManagementEnabled = TestData.facility(
      uuid = UUID.fromString("abe86f8e-1828-48fe-afb5-d697b3ce36bb"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
  )

  private val facilityWithDiabetesManagementDisabled = TestData.facility(
      uuid = UUID.fromString("abe86f8e-1828-48fe-afb5-d697b3ce36bb"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
  )

  private val facilityWithTeleconsultationEnabled = TestData.facility(
      uuid = UUID.fromString("e3582a1a-baed-4e1c-95e0-d8f0ad7a05a2"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true, teleconsultationEnabled = true)
  )

  private val updateSpec = UpdateSpec(PatientSummaryUpdate())

  @Test
  fun `when the current facility is loaded, update the UI`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("c2ac78df-0dcc-4a42-abc0-ebc2f89c68c6")
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentUserAndFacilityLoaded(user, facilityWithDiabetesManagementEnabled))
        .then(
            assertThatNext(
                hasModel(defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled).userLoggedInStatusLoaded(user.loggedInStatus)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the patient summary profile is loaded, then update the UI`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientSummaryProfileLoaded(patientSummaryProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when there are patient summary changes and at least one measurement is present, clicking on back must show the schedule appointment sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            countOfRecordedMeasurements = 1,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, BACK_CLICK, facilityWithDiabetesManagementEnabled) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and at least one measurement is present and no diagnosis is recorded and diabetes management is enabled, then clicking on back must show diagnosis error`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            countOfRecordedMeasurements = 1,
            diagnosisRecorded = false
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDiagnosisError as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and at least one measurement is present and no diagnosis is recorded and diabetes management is disabled, then clicking on back must show schedule appointment sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementDisabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            countOfRecordedMeasurements = 1,
            diagnosisRecorded = false
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, BACK_CLICK, facilityWithDiabetesManagementDisabled) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and no measurements are recorded, clicking on back for existing patient screen must go back to previous screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model.forExistingPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            countOfRecordedMeasurements = 0,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and no measurements are recorded, clicking on back for new patient screen must go back to home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model.forNewPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            countOfRecordedMeasurements = 0,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and no measurements are recorded, clicking on back link id with patient screen must go back to home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model.forLinkingWithExistingPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            countOfRecordedMeasurements = 0,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient summary changes and at least one measurement is recorded, clicking on back must go back`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = false,
            countOfRecordedMeasurements = 1,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient summary changes and no measurements are recorded, clicking on back must go back`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = false,
            countOfRecordedMeasurements = 0,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when at least one measurement is present, clicking on save must show the schedule appointment sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            countOfRecordedMeasurements = 1,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, DONE_CLICK, facilityWithDiabetesManagementEnabled) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when at least one measurement is present and diagnosis is not recorded and diabetes management is enabled, clicking on save must show diagnosis error`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            countOfRecordedMeasurements = 1,
            diagnosisRecorded = false
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDiagnosisError as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when at least one measurement is present and diagnosis is not recorded and diabetes management is disabled, clicking on save must show schedule appointment sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementDisabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            countOfRecordedMeasurements = 1,
            diagnosisRecorded = false
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, DONE_CLICK, facilityWithDiabetesManagementDisabled) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when no measurements are present, clicking on save must go to the home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            countOfRecordedMeasurements = 0,
            diagnosisRecorded = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when an appointment is scheduled, trigger a sync`() {
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(ScheduledAppointment(BACK_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(TriggerSync(BACK_CLICK) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the sync is triggered after clicking back from a new patient, go to home screen`() {
    val model = defaultModel
        .forNewPatient()
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(BACK_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoToHomeScreen as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the sync is triggered after clicking back from an existing patient, go to previous screen`() {
    val model = defaultModel
        .forExistingPatient()
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(BACK_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the sync is triggered after clicking back from linking id with patient, go to home screen`() {
    val model = defaultModel
        .forLinkingWithExistingPatient()
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(BACK_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoToHomeScreen as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the sync is triggered after clicking save from a new patient, go to home screen`() {
    val model = defaultModel
        .forNewPatient()
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(DONE_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoToHomeScreen as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the sync is triggered after clicking save from an existing patient, go to home screen`() {
    val model = defaultModel
        .forExistingPatient()
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(DONE_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoToHomeScreen as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the sync is triggered after clicking save from linking id with patient, go to home screen`() {
    val model = defaultModel
        .forLinkingWithExistingPatient()
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(DONE_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoToHomeScreen as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when a measurement is recorded for a new patient without phone number, do not show the missing phone reminder`() {
    val model = defaultModel
        .forNewPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()
        .withoutPhoneNumber()

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryBloodPressureSaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when a measurement is recorded for a new patient with phone number, do not show the missing phone reminder`() {
    val model = defaultModel
        .forNewPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryBloodPressureSaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when a measurement is recorded for an existing patient without phone number, show the missing phone reminder`() {
    val model = defaultModel
        .forExistingPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()
        .withoutPhoneNumber()

    updateSpec
        .given(model)
        .whenEvents(PatientSummaryBloodPressureSaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(FetchHasShownMissingPhoneReminder(patientUuid) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when a measurement is recorded for an existing patient with phone number, do not show the missing phone reminder`() {
    val model = defaultModel
        .forExistingPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryBloodPressureSaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when a measurement is recorded after linking BP passport for patient without phone number, show the missing phone reminder`() {
    val model = defaultModel
        .forLinkingWithExistingPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()
        .withoutPhoneNumber()

    updateSpec
        .given(model)
        .whenEvents(PatientSummaryBloodPressureSaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(FetchHasShownMissingPhoneReminder(patientUuid) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when a measurement is recorded after linking BP passport for patient with phone number, do not show the missing phone reminder`() {
    val model = defaultModel
        .forLinkingWithExistingPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryBloodPressureSaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the missing phone reminder has been shown for a patient, do not show it again`() {
    val model = defaultModel
        .forExistingPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(FetchedHasShownMissingPhoneReminder(true))
        .then(
            assertThatNext(
                hasNoModel(),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the missing phone reminder has not been shown for a patient, show it`() {
    val model = defaultModel
        .forExistingPatient()
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(FetchedHasShownMissingPhoneReminder(false))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowAddPhonePopup(patientUuid), MarkReminderAsShown(patientUuid))
            )
        )
  }

  @Test
  fun `when contact patient is clicked, open the contact patient screen`() {
    val model = defaultModel
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(ContactPatientClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenContactPatientScreen(patientUuid) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when contact doctor button is clicked, then open contact doctor sheet`() {
    val model = defaultModel
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(ContactDoctorClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenContactDoctorSheet(patientUuid) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when log teleconsult is clicked, then navigate to teleconsult record screen`() {
    val teleconsultRecordId = UUID.fromString("bc2545e7-de84-4aac-9c25-fc2a40cd96c6")
    val model = PatientSummaryModel.from(
        openIntention = OpenIntention.ViewExistingPatientWithTeleconsultLog(teleconsultRecordId),
        patientUuid = patientUuid
    )

    updateSpec
        .given(model)
        .whenEvent(LogTeleconsultClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(NavigateToTeleconsultRecordScreen(
                    patientUuid,
                    teleconsultRecordId
                ) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when medical officers are loaded, then update the model`() {
    val medicalOfficers = listOf(
        TestData.medicalOfficer(id = UUID.fromString("14edf8dc-41e6-4c35-954e-233c6853bc87")),
        TestData.medicalOfficer(id = UUID.fromString("a037fcd9-dab9-4df6-8608-c20e2cd934a3"))
    )
    val model = PatientSummaryModel.from(
        openIntention = ViewExistingPatient,
        patientUuid = patientUuid
    )

    updateSpec
        .given(model)
        .whenEvent(MedicalOfficersLoaded(medicalOfficers))
        .then(assertThatNext(
            hasModel(model.medicalOfficersLoaded(medicalOfficers)),
            hasNoEffects()
        ))
  }

  private fun PatientSummaryModel.forExistingPatient(): PatientSummaryModel {
    return copy(openIntention = ViewExistingPatient)
  }

  private fun PatientSummaryModel.forNewPatient(): PatientSummaryModel {
    return copy(openIntention = ViewNewPatient)
  }

  private fun PatientSummaryModel.forLinkingWithExistingPatient(): PatientSummaryModel {
    return copy(openIntention = LinkIdWithPatient(
        identifier = Identifier(
            value = "9588269e-7a2d-4a53-ba2e-32c1e9a5b8e3",
            type = BpPassport
        )
    ))
  }

  private fun PatientSummaryModel.withoutPhoneNumber(): PatientSummaryModel {
    return copy(patientSummaryProfile = patientSummaryProfile!!.copy(phoneNumber = null))
  }
}
