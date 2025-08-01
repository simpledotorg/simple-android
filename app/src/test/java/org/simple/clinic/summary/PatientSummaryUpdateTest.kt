package org.simple.clinic.summary

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.drugs.DiagnosisWarningPrescriptions
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.patient.Answer
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.reassignpatient.ReassignPatientSheetClosedFrom
import org.simple.clinic.reassignpatient.ReassignPatientSheetOpenedFrom
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.DONE_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.NEXT_APPOINTMENT_ACTION_CLICK
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import java.time.Instant
import java.util.UUID

class PatientSummaryUpdateTest {

  private val patientUuid = UUID.fromString("93a131b0-890e-41a3-88ec-b35b48efc6c5")
  private val defaultModel = PatientSummaryModel.from(ViewExistingPatient, patientUuid)

  private val patient = TestData.patient(patientUuid, status = PatientStatus.Active)
  private val patientAddress = TestData.patientAddress(patient.addressUuid)
  private val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
  private val bpPassportIdentifier = Identifier("526 780", BpPassport)
  private val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = bpPassportIdentifier)
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
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false
      )
  )

  private val facilityWithDiabetesManagementDisabled = TestData.facility(
      uuid = UUID.fromString("abe86f8e-1828-48fe-afb5-d697b3ce36bb"),
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = false,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false
      )
  )

  private val facilityWithTeleconsultationEnabled = TestData.facility(
      uuid = UUID.fromString("e3582a1a-baed-4e1c-95e0-d8f0ad7a05a2"),
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = true,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false
      )
  )

  private val updateSpec = UpdateSpec(PatientSummaryUpdate(
      isPatientReassignmentFeatureEnabled = true,
      isPatientStatinNudgeV1Enabled = true,
      isNonLabBasedStatinNudgeEnabled = true,
      isLabBasedStatinNudgeEnabled = true,
  ))

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
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = true,
        isPatientStatinNudgeV1Enabled = false,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = false,
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientSummaryProfileLoaded(patientSummaryProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the patient summary profile is loaded and statin nudge v1 feature flag is enabled, then update the UI and load statin check info`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = true,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = false,
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientSummaryProfileLoaded(patientSummaryProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)),
            hasEffects(LoadStatinPrescriptionCheckInfo(patient = patientSummaryProfile.patient))
        ))
  }

  @Test
  fun `when the patient summary profile is loaded and non lab based statin nudge feature flag is enabled, then update the UI and load statin check info`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = true,
        isPatientStatinNudgeV1Enabled = false,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = false,
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientSummaryProfileLoaded(patientSummaryProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)),
            hasEffects(LoadStatinPrescriptionCheckInfo(patient = patientSummaryProfile.patient))
        ))
  }

  @Test
  fun `when the patient summary profile is loaded and lab based statin nudge feature flag is enabled, then update the UI and load statin check info`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = true,
        isPatientStatinNudgeV1Enabled = false,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientSummaryProfileLoaded(patientSummaryProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)),
            hasEffects(LoadStatinPrescriptionCheckInfo(patient = patientSummaryProfile.patient))
        ))
  }

  @Test
  fun `when the patient summary profile is loaded and link id with patient is present, then show link id sheet`() {
    val linkIdWithPatientModel = PatientSummaryModel.from(LinkIdWithPatient(bpPassportIdentifier), patientUuid)
    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        alternativeId = bangladeshNationalId,
        facility = facility,
        bpPassport = null
    )

    updateSpec
        .given(linkIdWithPatientModel)
        .whenEvent(PatientSummaryProfileLoaded(patientSummaryProfile))
        .then(assertThatNext(
            hasModel(linkIdWithPatientModel.patientSummaryProfileLoaded(patientSummaryProfile)),
            hasEffects(
                ShowLinkIdWithPatientView(patientUuid, bpPassportIdentifier)
            )
        ))
  }

  @Test
  fun `when there are patient summary changes and appointment is not changed, clicking on back must show the schedule appointment sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
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
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("1d686e68-b64b-40ae-ba33-614c81853389"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Unanswered,
                hasDiabetes = Unanswered
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
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
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("9da22d50-9a5e-4c78-b451-39cc8535fec9"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Unanswered,
                hasDiabetes = Unanswered
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, BACK_CLICK, facilityWithDiabetesManagementDisabled) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient measurement changes and no warnings, clicking on back for existing patient screen must go back to previous screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .shownMeasurementsWarningDialog()

    updateSpec
        .given(model.forExistingPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("b29992e8-9dfe-4499-84a6-31c0d546c312"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient measurement changes and no warnings, clicking on back for new patient screen must go back to home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .shownMeasurementsWarningDialog()

    updateSpec
        .given(model.forNewPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("c2bda975-f7b6-4e23-a53f-82acb55e9f37"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient measurement changes and warnings, clicking on back link id with patient screen must go back to home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .shownMeasurementsWarningDialog()

    updateSpec
        .given(model.forLinkingWithExistingPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("c7d5d852-de82-4f80-95dc-611e333d370b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
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
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("95df9f42-2fd0-4429-b2ad-ae3de909a480"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient summary changes and no measurements are recorded and warning is shown, clicking on back must go back`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .shownMeasurementsWarningDialog()

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("f4825cd5-10ac-4494-af8b-a616377b42df"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when patient measurement data is changed and appointment is not changed, clicking on save must show the schedule appointment sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("7aeb58c1-19f8-43f8-952c-8fb069b9268b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
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
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("13c1b8b9-7109-4b2f-ad01-3532f9be6766"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Unanswered,
                hasDiabetes = Unanswered
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
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
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("e6514821-3fb4-4b78-b20a-e8ab6f856c0b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Unanswered,
                hasDiabetes = Unanswered
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, DONE_CLICK, facilityWithDiabetesManagementDisabled) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when no measurements are present and measurement warning dialogs are shown, clicking on save must go to the home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .shownMeasurementsWarningDialog()

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("a00b6a43-b0e1-4f07-8d17-ad99e17f482d"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
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

  @Test
  fun `when patient is diagnosed with hypertension and diabetes and has no recorded measurements, then clicking on done should show add measurements warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("4fd4be06-6208-4b79-b153-1a2ef4c2024b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasModel(model.shownMeasurementsWarningDialog()),
            hasEffects(ShowAddMeasurementsWarningDialog)
        ))
  }

  @Test
  fun `when patient is diagnosed with hypertension and has no recorded measurements, then clicking on done should show add bp warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("4fd4be06-6208-4b79-b153-1a2ef4c2024b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasModel(model.shownMeasurementsWarningDialog()),
            hasEffects(ShowAddBloodPressureWarningDialog)
        ))
  }

  @Test
  fun `when patient is diagnosed with diabetes and has no recorded measurements, then clicking on done should show add blood sugar warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("bdc93463-1577-42df-a6ff-b0526dc0b680"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasModel(model.shownMeasurementsWarningDialog()),
            hasEffects(ShowAddBloodSugarWarningDialog)
        ))
  }

  @Test
  fun `when patient is diagnosed with diabetes and has no recorded measurements and diabetes management is not enabled for facility, then clicking on done should show schedule appointment sheet if measurements are changed`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementDisabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("bdc93463-1577-42df-a6ff-b0526dc0b680"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(
                patientUuid = patientUuid,
                sheetOpenedFrom = DONE_CLICK,
                currentFacility = facilityWithDiabetesManagementDisabled
            ))
        ))
  }

  @Test
  fun `when patient is diagnosed with hypertension and diabetes and has no recorded measurements, then clicking on back should show add measurements warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("8f96e650-0ec2-4128-98a2-f4ca0c93bdf9"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasModel(model.shownMeasurementsWarningDialog()),
            hasEffects(ShowAddMeasurementsWarningDialog)
        ))
  }

  @Test
  fun `when patient is diagnosed with hypertension and has no recorded measurements, then clicking on back should show add bp warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("909d198e-c905-4d07-aa39-d209be63765e"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasModel(model.shownMeasurementsWarningDialog()),
            hasEffects(ShowAddBloodPressureWarningDialog)
        ))
  }

  @Test
  fun `when patient is diagnosed with diabetes and has no recorded measurements, then clicking on back should show add blood sugar warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("909d198e-c905-4d07-aa39-d209be63765e"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasModel(model.shownMeasurementsWarningDialog()),
            hasEffects(ShowAddBloodSugarWarningDialog)
        ))
  }

  @Test
  fun `when patient is diagnosed with diabetes and has no recorded measurements and diabetes management is not enabled for facility, then clicking on back should show schedule appointment sheet if measurements are changed`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementDisabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("909d198e-c905-4d07-aa39-d209be63765e"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(
                patientUuid = patientUuid,
                sheetOpenedFrom = BACK_CLICK,
                currentFacility = facilityWithDiabetesManagementDisabled
            ))
        ))
  }

  @Test
  fun `when nurse tries to change the patient assigned facility, open the select facility screen`() {
    val model = defaultModel
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(ChangeAssignedFacilityClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectFacilitySheet)
        ))
  }

  @Test
  fun `when nurse selects the new assigned facility, dispatch the newly selected facility`() {
    val model = defaultModel
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(NewAssignedFacilitySelected(facilityWithDiabetesManagementEnabled))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(DispatchNewAssignedFacility(facilityWithDiabetesManagementEnabled))
        ))
  }

  @Test
  fun `when back is clicked and patient is dead, then go back to previous screen`() {
    val patientUuid = UUID.fromString("c28e15d1-c83c-4d07-a839-b978e4482f30")
    val patient = TestData.patient(
        uuid = patientUuid,
        status = PatientStatus.Dead
    )

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )

    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryBackClicked(patientUuid, Instant.parse("2018-01-01T00:00:00Z")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen)
        ))
  }

  @Test
  fun `when back is clicked and patient is not dead, then check patient reassignment status`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryBackClicked(patientUuid, Instant.parse("2018-01-01T00:00:00Z")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CheckPatientReassignmentStatus(
                patientUuid = patientUuid,
                clickAction = ClickAction.BACK,
                screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
            ))
        ))
  }

  @Test
  fun `when done is clicked and patient is dead, then go back to previous screen`() {
    val patientUuid = UUID.fromString("c28e15d1-c83c-4d07-a839-b978e4482f30")
    val patient = TestData.patient(
        uuid = patientUuid,
        status = PatientStatus.Dead
    )

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )

    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryDoneClicked(patientUuid, Instant.parse("2018-01-01T00:00:00Z")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen)
        ))
  }

  @Test
  fun `when done is clicked and patient is not dead, then check patient reassignment status`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryDoneClicked(patientUuid, Instant.parse("2018-01-01T00:00:00Z")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CheckPatientReassignmentStatus(
                patientUuid = patientUuid,
                clickAction = ClickAction.DONE,
                screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
            ))
        ))
  }

  @Test
  fun `when patient phone number is invalid, then show update phone popup`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CompletedCheckForInvalidPhone(isPhoneInvalid = true))
        .then(assertThatNext(
            hasModel(defaultModel.completedCheckForInvalidPhone()),
            hasEffects(ShowUpdatePhonePopup(patientUuid))
        ))
  }

  @Test
  fun `when patient registration data is loaded and has registration data, then update model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientRegistrationDataLoaded(
            countOfPrescribedDrugs = 2,
            countOfRecordedBloodPressures = 2,
            countOfRecordedBloodSugars = 0
        ))
        .then(assertThatNext(
            hasModel(defaultModel.patientRegistrationDataLoaded(hasPatientRegistrationData = true)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when patient registration data is loaded and doesn't has registration data, then update model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientRegistrationDataLoaded(
            countOfPrescribedDrugs = 0,
            countOfRecordedBloodPressures = 0,
            countOfRecordedBloodSugars = 0
        ))
        .then(assertThatNext(
            hasModel(defaultModel.patientRegistrationDataLoaded(hasPatientRegistrationData = false)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when sync is triggered after clicking change from next appointment, then refresh appointment`() {
    val model = defaultModel
        .forExistingPatient()
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()

    updateSpec
        .given(model)
        .whenEvent(SyncTriggered(NEXT_APPOINTMENT_ACTION_CLICK))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(RefreshNextAppointment)
            )
        )
  }

  @Test
  fun `when next appointment action button is clicked, then open schedule appointment sheet`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(NextAppointmentActionClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, NEXT_APPOINTMENT_ACTION_CLICK, facility))
        ))
  }

  @Test
  fun `when patient measurement data is not changed and there are no diagnosis and measurement warning, clicking on done must take them back to home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = false,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("7aeb58c1-19f8-43f8-952c-8fb069b9268b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen)
        ))
  }

  @Test
  fun `when patient measurement data is changed and appointment is changed, clicking on save must go to home screen`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = true,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("7aeb58c1-19f8-43f8-952c-8fb069b9268b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen)
        ))
  }

  @Test
  fun `when viewing existing patient and patient measurement data is changed and appointment is changed, clicking on back must go to previous screen`() {
    val model = PatientSummaryModel
        .from(
            openIntention = ViewExistingPatient,
            patientUuid = patientUuid
        )
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = true,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("7aeb58c1-19f8-43f8-952c-8fb069b9268b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen)
        ))
  }

  @Test
  fun `when viewing new patient and patient measurement data is changed and appointment is changed, clicking on back must go to previous screen`() {
    val model = PatientSummaryModel
        .from(
            openIntention = ViewNewPatient,
            patientUuid = patientUuid
        )
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = true,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("7aeb58c1-19f8-43f8-952c-8fb069b9268b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen)
        ))
  }

  @Test
  fun `when viewing existing patient to link ID and patient measurement data is changed and appointment is changed, clicking on back must go to previous screen`() {
    val model = PatientSummaryModel
        .from(
            openIntention = LinkIdWithPatient(
                identifier = TestData.identifier(
                    value = "4574355a-3bd6-48c6-8d46-50eae707e1d3",
                    type = BpPassport
                )
            ),
            patientUuid = patientUuid
        )
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = true,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 0,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("7aeb58c1-19f8-43f8-952c-8fb069b9268b"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen)
        ))
  }

  @Test
  fun `when assigned facility changed, then refresh next appointment`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(AssignedFacilityChanged)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RefreshNextAppointment)
        ))
  }

  @Test
  fun `when cdss pilot is enabled for facility, then load clinical decision support info and latest scheduled appointment`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CDSSPilotStatusChecked(isPilotEnabledForFacility = true))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                LoadClinicalDecisionSupportInfo(patientUuid),
                LoadLatestScheduledAppointment(patientUuid)
            )
        ))
  }

  @Test
  fun `when cdss pilot is not enabled for facility, then do nothing`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CDSSPilotStatusChecked(isPilotEnabledForFacility = false))
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }

  @Test
  fun `when scheduled appointment is loaded, then update the model`() {
    val appointment = TestData.appointment(
        uuid = UUID.fromString("adec28f7-0761-4514-b036-2737d9a6064b")
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(LatestScheduledAppointmentLoaded(appointment))
        .then(assertThatNext(
            hasModel(defaultModel.scheduledAppointmentLoaded(appointment)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when clinical decision support info is loaded, then updated the model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(ClinicalDecisionSupportInfoLoaded(isNewestBpEntryHigh = true, hasPrescribedDrugsChangedToday = false))
        .then(assertThatNext(
            hasModel(defaultModel.clinicalDecisionSupportInfoLoaded(
                isNewestBpEntryHigh = true,
                hasPrescribedDrugsChangedToday = false
            )),
            hasNoEffects()
        ))
  }

  @Test
  fun `when measurement warning dialog not now is clicked and patient is dead, then go back to previous screen`() {
    val patientUuid = UUID.fromString("fb2a4f44-2df6-429c-9fc4-954ddea963bc")
    val patient = TestData.patient(
        uuid = patientUuid,
        status = PatientStatus.Dead
    )

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )

    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(MeasurementWarningNotNowClicked(patientUuid, Instant.parse("2018-01-01T00:00:00Z")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen)
        ))
  }

  @Test
  fun `when measurement warning dialog not now is clicked and patient is not dead, the check patient reassignment status`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(MeasurementWarningNotNowClicked(patientUuid, Instant.parse("2018-01-01T00:00:00Z")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CheckPatientReassignmentStatus(
                patientUuid = patientUuid,
                clickAction = ClickAction.BACK,
                screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            ))
        ))
  }

  @Test
  fun `when patient reassignment status is loaded and click action is done, then update status and load data for done click`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientReassignmentStatusLoaded(
            isPatientEligibleForReassignment = true,
            clickAction = ClickAction.DONE,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                UpdatePatientReassignmentStatus(patientUuid, status = Answer.Yes),
                LoadDataForDoneClick(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    canShowPatientReassignmentWarning = true
                )
            )
        ))
  }

  @Test
  fun `when patient reassignment status is loaded and click action is back, then update status and load data for back click`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientReassignmentStatusLoaded(
            isPatientEligibleForReassignment = false,
            clickAction = ClickAction.BACK,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                UpdatePatientReassignmentStatus(patientUuid, status = Answer.No),
                LoadDataForBackClick(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    canShowPatientReassignmentWarning = false
                )
            )
        ))
  }

  @Test
  fun `when viewing existing patient and patient can be reassigned and save is clicked, then show reassign patient sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowReassignPatientWarningSheet(
                patientUuid = patientUuid,
                currentFacility = facilityWithDiabetesManagementEnabled,
                sheetOpenedFrom = ReassignPatientSheetOpenedFrom.DONE_CLICK
            ))
        ))
  }

  @Test
  fun `when viewing existing patient and patient can be reassigned and back is clicked, then show reassign patient sheet`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = emptyList(),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions.empty()
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowReassignPatientWarningSheet(
                patientUuid = patientUuid,
                currentFacility = facilityWithDiabetesManagementEnabled,
                sheetOpenedFrom = ReassignPatientSheetOpenedFrom.DONE_CLICK
            ))
        ))
  }

  @Test
  fun `when patient reassignment feature is disabled, and patient is not dead, and done is clicked, then load data for done click`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = false,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = false,
    ))
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryDoneClicked(
            patientUuid = patientUuid,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                LoadDataForDoneClick(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    canShowPatientReassignmentWarning = false
                )
            )
        ))
  }

  @Test
  fun `when patient reassignment feature is disabled, and patient is not dead, and back is clicked, then load data for back click`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = false,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = false,
    ))
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(PatientSummaryBackClicked(
            patientUuid = patientUuid,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                LoadDataForBackClick(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    canShowPatientReassignmentWarning = false
                )
            )
        ))
  }

  @Test
  fun `when patient reassignment feature is disabled and measurement warning not now is clicked, then load data for back click`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = false,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = false,
    ))
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(MeasurementWarningNotNowClicked(
            patientUuid = patientUuid,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                LoadDataForBackClick(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    canShowPatientReassignmentWarning = false
                )
            )
        ))
  }

  @Test
  fun `when patient reassignment sheet is closed with not now, and sheet is opened by done click, then load done click data`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientReassignmentWarningClosed(
            patientUuid = patientUuid,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            sheetOpenedFrom = ReassignPatientSheetOpenedFrom.DONE_CLICK,
            sheetClosedFrom = ReassignPatientSheetClosedFrom.NOT_NOW
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                LoadDataForDoneClick(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    canShowPatientReassignmentWarning = false
                )
            )
        ))
  }

  @Test
  fun `when patient reassignment sheet is closed with not now, and sheet is opened by back click, then load back click data`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientReassignmentWarningClosed(
            patientUuid = patientUuid,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            sheetOpenedFrom = ReassignPatientSheetOpenedFrom.BACK_CLICK,
            sheetClosedFrom = ReassignPatientSheetClosedFrom.NOT_NOW
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                LoadDataForBackClick(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    canShowPatientReassignmentWarning = false
                )
            )
        ))
  }

  @Test
  fun `when patient reassignment sheet is closed with change, and sheet is opened by done click, then check patient reassignment status`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientReassignmentWarningClosed(
            patientUuid = patientUuid,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            sheetOpenedFrom = ReassignPatientSheetOpenedFrom.DONE_CLICK,
            sheetClosedFrom = ReassignPatientSheetClosedFrom.CHANGE
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                CheckPatientReassignmentStatus(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    clickAction = ClickAction.DONE
                )
            )
        ))
  }

  @Test
  fun `when patient reassignment sheet is closed with change, and sheet is opened by back click, then check patient reassignment status`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientReassignmentWarningClosed(
            patientUuid = patientUuid,
            screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            sheetOpenedFrom = ReassignPatientSheetOpenedFrom.BACK_CLICK,
            sheetClosedFrom = ReassignPatientSheetClosedFrom.CHANGE
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                CheckPatientReassignmentStatus(
                    patientUuid = patientUuid,
                    screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
                    clickAction = ClickAction.BACK
                )
            )
        ))
  }

  @Test
  fun `when has diabetes is clicked, then mark the diabetes diagnosis`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(HasDiabetesClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkDiabetesDiagnosis(patientUuid))
        ))
  }

  @Test
  fun `when has hypertension is clicked and doesn't have diabetes warning, then mark the hypertension diagnosis`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(HasHypertensionClicked(continueToDiabetesDiagnosisWarning = false))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkHypertensionDiagnosis(patientUuid))
        ))
  }

  @Test
  fun `when has hypertension is clicked and also have diabetes warning, then mark the hypertension diagnosis and show diabetes diagnosis warning`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(HasHypertensionClicked(continueToDiabetesDiagnosisWarning = true))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkHypertensionDiagnosis(patientUuid), ShowDiabetesDiagnosisWarning)
        ))
  }

  @Test
  fun `when hypertension diagnosis not now is clicked and also have diabetes warning, then show diabetes diagnosis warning`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(HypertensionNotNowClicked(continueToDiabetesDiagnosisWarning = true))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDiabetesDiagnosisWarning)
        ))
  }

  @Test
  fun `when hypertension diagnosis not now is clicked and doesn't have diabetes warning, then do nothing`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(HypertensionNotNowClicked(continueToDiabetesDiagnosisWarning = false))
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }

  @Test
  fun `when data for done click is loaded and can show hypertension diagnosis warning dialog, then show hypertension diagnosis warning dialog`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = listOf(
                TestData.prescription(
                    uuid = UUID.fromString("d688f42f-17fd-4792-8e04-95681edd481d"),
                    name = "Amlodipine"
                )
            ),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
                htnPrescriptions = listOf("amlodipine"),
                diabetesPrescriptions = emptyList()
            )
        ))
        .then(assertThatNext(
            hasModel(model.shownDiagnosisWarningDialog()),
            hasEffects(ShowHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning = false))
        ))
  }

  @Test
  fun `when data for done click is loaded and can show diabetes diagnosis warning dialog, then show diabetes diagnosis warning dialog`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = listOf(
                TestData.prescription(
                    uuid = UUID.fromString("d688f42f-17fd-4792-8e04-95681edd481d"),
                    name = "Metformin"
                )
            ),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
                htnPrescriptions = emptyList(),
                diabetesPrescriptions = listOf("metformin")
            )
        ))
        .then(assertThatNext(
            hasModel(model.shownDiagnosisWarningDialog()),
            hasEffects(ShowDiabetesDiagnosisWarning)
        ))
  }

  @Test
  fun `when data for done click is loaded and can show HTN and diabetes diagnosis warning dialog, then show hypetension diagnosis warning dialog continued by diabetes diagnosis warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = listOf(
                TestData.prescription(
                    uuid = UUID.fromString("3f742f92-3d6b-4754-bdea-eed4fcfa440f"),
                    name = "Amlodipine"
                ),
                TestData.prescription(
                    uuid = UUID.fromString("d688f42f-17fd-4792-8e04-95681edd481d"),
                    name = "Metformin"
                )
            ),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
                htnPrescriptions = listOf("amlodipine"),
                diabetesPrescriptions = listOf("metformin")
            )
        ))
        .then(assertThatNext(
            hasModel(model.shownDiagnosisWarningDialog()),
            hasEffects(ShowHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning = true))
        ))
  }

  @Test
  fun `when data for back click is loaded and can show hypertension diagnosis warning dialog, then show hypertension diagnosis warning dialog`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = Yes
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = listOf(
                TestData.prescription(
                    uuid = UUID.fromString("d688f42f-17fd-4792-8e04-95681edd481d"),
                    name = "Amlodipine"
                )
            ),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
                htnPrescriptions = listOf("amlodipine"),
                diabetesPrescriptions = emptyList()
            )
        ))
        .then(assertThatNext(
            hasModel(model.shownDiagnosisWarningDialog()),
            hasEffects(ShowHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning = false))
        ))
  }

  @Test
  fun `when data for back click is loaded and can show diabetes diagnosis warning dialog, then show diabetes diagnosis warning dialog`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = Yes,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = listOf(
                TestData.prescription(
                    uuid = UUID.fromString("d688f42f-17fd-4792-8e04-95681edd481d"),
                    name = "Metformin"
                )
            ),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
                htnPrescriptions = emptyList(),
                diabetesPrescriptions = listOf("metformin")
            )
        ))
        .then(assertThatNext(
            hasModel(model.shownDiagnosisWarningDialog()),
            hasEffects(ShowDiabetesDiagnosisWarning)
        ))
  }

  @Test
  fun `when data for back click is loaded and can show HTN and diabetes diagnosis warning dialog, then show hypetension diagnosis warning dialog continued by diabetes diagnosis warning`() {
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    updateSpec
        .given(model)
        .whenEvent(DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 1,
            countOfRecordedBloodSugars = 1,
            medicalHistory = TestData.medicalHistory(
                uuid = UUID.fromString("94056dc9-85e9-472e-8674-1657bbab56bb"),
                patientUuid = patientUuid,
                diagnosedWithHypertension = No,
                hasDiabetes = No
            ),
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = listOf(
                TestData.prescription(
                    uuid = UUID.fromString("3f742f92-3d6b-4754-bdea-eed4fcfa440f"),
                    name = "Amlodipine"
                ),
                TestData.prescription(
                    uuid = UUID.fromString("d688f42f-17fd-4792-8e04-95681edd481d"),
                    name = "Metformin"
                )
            ),
            diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
                htnPrescriptions = listOf("amlodipine"),
                diabetesPrescriptions = listOf("metformin")
            )
        ))
        .then(assertThatNext(
            hasModel(model.shownDiagnosisWarningDialog()),
            hasEffects(ShowHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning = true))
        ))
  }

  @Test
  fun `when statin prescription check info is loaded and person is below 40 without cvd, then update the state with false`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = false,
    ))
    updateSpec
        .given(defaultModel)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 39,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(StatinInfo(canShowStatinNudge = false, hasCVD = false))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when statin prescription check info is loaded and person has cvd, then update the state with true`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = false,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = false,
    ))
    updateSpec
        .given(defaultModel)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 39,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = Yes,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(StatinInfo(canShowStatinNudge = true, hasCVD = true, hasDiabetes = false, age = 39))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when statin prescription check info is loaded and person is greater than or equal to 40 with diabetes, then update the state with true`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = false,
    ))
    updateSpec
        .given(defaultModel)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 40,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = Yes,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(StatinInfo(canShowStatinNudge = true, hasCVD = false, hasDiabetes = true, age = 40))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when statin prescription check info is loaded and person is above 40 with null cvd risk, then calculate cvd risk`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = false,
    ))

    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 40,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = true,
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CalculateNonLabBasedCVDRisk(model.patientSummaryProfile!!.patient))
        ))
  }

  @Test
  fun `when statin prescription check info is loaded and person is above 40 with cvd risk, then load statin info`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = false,
    ))
    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 48,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = CVDRiskRange(14, 21),
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = true,
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadStatinInfo(patientUuid))
        ))
  }

  @Test
  fun `when cvd risk score is calculated and old cvd risk is null, then save cvd risk`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CVDRiskCalculated(
            oldRisk = null,
            newRiskRange = CVDRiskRange(5, 14)
        ))
        .then(assertThatNext(
            hasEffects(SaveCVDRisk(patientUuid, CVDRiskRange(5, 14)))
        ))
  }

  @Test
  fun `when cvd risk score is calculated and old cvd risk is not null, then save cvd risk`() {
    val existingCvdRisk = TestData.cvdRisk(riskScore = CVDRiskRange(5, 14))
    updateSpec
        .given(defaultModel)
        .whenEvent(CVDRiskCalculated(
            oldRisk = existingCvdRisk,
            newRiskRange = CVDRiskRange(5, 14)
        ))
        .then(assertThatNext(
            hasEffects(UpdateCVDRisk(existingCvdRisk, CVDRiskRange(5, 14)))
        ))
  }

  @Test
  fun `when cvd risk score is calculated and both range and old cvd risk are null, then load statin info`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CVDRiskCalculated(
            oldRisk = null,
            newRiskRange = null
        ))
        .then(assertThatNext(
            hasEffects(LoadStatinInfo(defaultModel.patientUuid))
        ))
  }

  @Test
  fun `when statin info is loaded, then update the state`() {
    val statinInfo = StatinInfo(
        canShowStatinNudge = true,
        cvdRisk = CVDRiskRange(11, 27),
        isSmoker = Yes,
        bmiReading = BMIReading(165f, 60f),
        hasCVD = true,
        hasDiabetes = false,
        age = 55,
        cholesterol = null,
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(StatinInfoLoaded(
            age = 55,
            medicalHistory = TestData.medicalHistory(
                hasHadStroke = Yes,
                hasHadHeartAttack = Yes,
                hasDiabetes = No,
                isSmoking = Yes,
                cholesterol = null,
            ),
            canPrescribeStatin = true,
            riskRange = CVDRiskRange(11, 27),
            bmiReading = BMIReading(165f, 60f),
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(statinInfo)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when statin info is loaded and lab based statin nudge is not enabled and max risk is below 10, then statin cannot be prescribed`() {
    val statinInfo = StatinInfo(
        canShowStatinNudge = false,
        cvdRisk = CVDRiskRange(9, 9),
        isSmoker = Yes,
        bmiReading = BMIReading(165f, 60f),
        hasCVD = true,
        hasDiabetes = false,
        age = 55,
        cholesterol = null,
    )
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = true,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = false,
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(StatinInfoLoaded(
            age = 55,
            medicalHistory = TestData.medicalHistory(
                hasHadStroke = Yes,
                hasHadHeartAttack = Yes,
                hasDiabetes = No,
                isSmoking = Yes,
                cholesterol = null,
            ),
            canPrescribeStatin = false,
            riskRange = CVDRiskRange(9, 9),
            bmiReading = BMIReading(165f, 60f),
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(statinInfo)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when statin info is loaded and lab-based statin is enabled and has diabetes and max risk is less than 10, then statin cannot be prescribed`() {
    val statinInfo = StatinInfo(
        canShowStatinNudge = false,
        cvdRisk = null,
        isSmoker = Yes,
        bmiReading = BMIReading(165f, 60f),
        hasCVD = true,
        hasDiabetes = false,
        age = 55,
        cholesterol = null,
    )
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = true,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = true,
        isLabBasedStatinNudgeEnabled = true,
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(StatinInfoLoaded(
            age = 55,
            medicalHistory = TestData.medicalHistory(
                hasHadStroke = Yes,
                hasHadHeartAttack = Yes,
                hasDiabetes = No,
                isSmoking = Yes,
                cholesterol = null,
            ),
            canPrescribeStatin = false,
            riskRange = null,
            bmiReading = BMIReading(165f, 60f),
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(statinInfo)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when statin info is loaded and risk is low-high, then update the state and show smoking status dialog`() {
    val statinInfo = StatinInfo(
        canShowStatinNudge = true,
        cvdRisk = CVDRiskRange(4, 27),
        isSmoker = Unanswered,
        bmiReading = BMIReading(165f, 60f),
        hasCVD = true,
        hasDiabetes = false,
        age = 55,
        cholesterol = null,
    )
    updateSpec
        .given(defaultModel)
        .whenEvent(StatinInfoLoaded(
            age = 55,
            medicalHistory = TestData.medicalHistory(
                hasHadStroke = Yes,
                hasHadHeartAttack = Yes,
                hasDiabetes = No,
                isSmoking = Unanswered,
                cholesterol = null,
            ),
            canPrescribeStatin = true,
            riskRange = CVDRiskRange(4, 27),
            bmiReading = BMIReading(165f, 60f),
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(statinInfo).showTobaccoUseDialog()),
            hasEffects(ShowTobaccoStatusDialog)
        ))
  }

  @Test
  fun `when add smoking button is clicked, then show the smoking status dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(AddTobaccoUseClicked)
        .then(assertThatNext(
            hasEffects(ShowTobaccoStatusDialog),
            hasNoModel()
        ))
  }

  @Test
  fun `when smoking is answered, then update the smoking status`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(SmokingStatusAnswered(
            isSmoker = No
        ))
        .then(assertThatNext(
            hasEffects(UpdateSmokingStatus(patientId = patientUuid, isSmoker = No)),
            hasNoModel()
        ))
  }

  @Test
  fun `when add bmi button is clicked, then open the bmi entry sheet`() {
    val model = defaultModel
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(AddBMIClicked)
        .then(assertThatNext(
            hasEffects(OpenBMIEntrySheet(model.patientUuid)),
            hasNoModel()
        ))
  }

  @Test
  fun `when BMI reading is added, then calculate the cvd risk`() {
    val model = defaultModel
        .patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(BMIReadingAdded)
        .then(assertThatNext(
            hasEffects(CalculateNonLabBasedCVDRisk(patientSummaryProfile.patient)),
            hasNoModel()
        ))
  }

  @Test
  fun `when lab based statin feature is enabled and statin prescription check info is loaded and person is below 40 without cvd, then update the state with false`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))
    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 39,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasModel(model.updateStatinInfo(StatinInfo(canShowStatinNudge = false, hasDiabetes = false))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when lab based statin feature is enabled statin prescription check info is loaded and person has cvd, then update the state with true`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))
    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 39,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = Yes,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasModel(model.updateStatinInfo(StatinInfo(canShowStatinNudge = true, hasCVD = true))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when lab based statin feature is enabled statin prescription check info is loaded and person has diabetes and age is greater than 74, then update the state with true`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))
    updateSpec
        .given(defaultModel)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 75,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = Yes,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateStatinInfo(StatinInfo(canShowStatinNudge = true, hasDiabetes = true))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when lab based statin feature is enabled statin prescription check info is loaded and has diabetes and is eligible for calculating lab based CVD risk and should calculate CVD risk then calculate CVD risk`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))

    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 40,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = Yes,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = true,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CalculateLabBasedCVDRisk(model.patientSummaryProfile!!.patient))
        ))
  }

  @Test
  fun `when lab based statin feature is enabled statin prescription check info is loaded and doesnt have diabetes and is eligible for calculating lab based CVD risk and should calculate CVD risk then calculate CVD risk`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))

    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 40,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = null,
            hasMedicalHistoryChanged = true,
            wasCVDCalculatedWithin90Days = false,
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CalculateLabBasedCVDRisk(model.patientSummaryProfile!!.patient))
        ))
  }

  @Test
  fun `when lab based statin feature is enabled and statin prescription check info is loaded and person is above 40 with diabetes and is eligible for lab based CVD risk, then load statin info`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))
    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(StatinPrescriptionCheckInfoLoaded(
            age = 48,
            isPatientDead = false,
            wasBPMeasuredWithin90Days = true,
            medicalHistory = TestData.medicalHistory(
                hasDiabetes = No,
                hasHadStroke = No,
                hasHadHeartAttack = No,
            ),
            patientAttribute = null,
            prescriptions = listOf(
                TestData.prescription(name = "losartin")
            ),
            cvdRiskRange = CVDRiskRange(14, 21),
            hasMedicalHistoryChanged = false,
            wasCVDCalculatedWithin90Days = true,
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadStatinInfo(patientUuid))
        ))
  }

  @Test
  fun `when add cholesterol is clicked, then open cholesterol entry sheet`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(AddCholesterolClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenCholesterolEntrySheet(patientUuid))
        ))
  }

  @Test
  fun `when cholesterol is added, then calculate lab based cvd risk`() {
    val updateSpec = UpdateSpec(PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = false,
        isPatientStatinNudgeV1Enabled = true,
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
    ))
    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    updateSpec
        .given(model)
        .whenEvent(CholesterolAdded)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CalculateLabBasedCVDRisk(patient))
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
