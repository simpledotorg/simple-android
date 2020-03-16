package org.simple.clinic.summary

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.PatientMocker
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

  private val patient = PatientMocker.patient(patientUuid)
  private val patientAddress = PatientMocker.address(patient.addressUuid)
  private val phoneNumber = PatientMocker.phoneNumber(patientUuid = patientUuid)
  private val bpPassport = PatientMocker.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", BpPassport))
  private val bangladeshNationalId = PatientMocker.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", BangladeshNationalId))

  private val patientSummaryProfile = PatientSummaryProfile(
      patient = patient,
      address = patientAddress,
      phoneNumber = phoneNumber,
      bpPassport = bpPassport,
      bangladeshNationalId = bangladeshNationalId
  )

  private val facility = PatientMocker.facility(
      uuid = UUID.fromString("abe86f8e-1828-48fe-afb5-d697b3ce36bb"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
  )

  private val updateSpec = UpdateSpec(PatientSummaryUpdate())

  @Test
  fun `when the current facility is loaded, update the UI`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(
            assertThatNext(
                hasModel(defaultModel.currentFacilityLoaded(facility)),
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
  fun `when there are patient summary changes and at least one blood sugar is present, clicking on back must show the schedule appointment sheet`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = false,
            countOfRecordedMeasurements = 0
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, BACK_CLICK) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and all blood sugars are deleted, clicking on back for existing patient screen must go back to previous screen`() {
    updateSpec
        .given(defaultModel.forExistingPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = true,
            countOfRecordedMeasurements = 0
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and all blood sugars are deleted, clicking on back for new patient screen must go back to home screen`() {
    updateSpec
        .given(defaultModel.forNewPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = true,
            countOfRecordedMeasurements = 0
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are patient summary changes and all blood sugars are deleted, clicking on back link id with patient screen must go back to home screen`() {
    updateSpec
        .given(defaultModel.forLinkingWithExistingPatient())
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = true,
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = true,
            countOfRecordedMeasurements = 0
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient summary changes and all blood sugars are not deleted, clicking on back must go back`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = false,
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = false,
            countOfRecordedMeasurements = 0
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when there are no patient summary changes and all blood sugars are deleted, clicking on back must go back`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DataForBackClickLoaded(
            hasPatientDataChangedSinceScreenCreated = false,
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = true,
            countOfRecordedMeasurements = 0
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackToPreviousScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when all blood sugars are not deleted, clicking on save must show the schedule appointment sheet regardless of summary changes`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DataForDoneClickLoaded(
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = false
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowScheduleAppointmentSheet(patientUuid, DONE_CLICK) as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when all blood sugar are deleted, clicking on save must go to the home screen regardless of summary changes`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DataForDoneClickLoaded(
            noBloodPressuresRecordedForPatient = true,
            noBloodSugarsRecordedForPatient = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToHomeScreen as PatientSummaryEffect)
        ))
  }

  @Test
  fun `when an appointment is scheduled, trigger a sync`() {
    val model = defaultModel
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()
        .reportedViewedPatientToAnalytics()

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
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()
        .reportedViewedPatientToAnalytics()

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
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()
        .reportedViewedPatientToAnalytics()

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
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()
        .reportedViewedPatientToAnalytics()

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
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()
        .reportedViewedPatientToAnalytics()

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
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()
        .reportedViewedPatientToAnalytics()

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
        .currentFacilityLoaded(facility)
        .patientSummaryProfileLoaded(patientSummaryProfile)
        .completedCheckForInvalidPhone()
        .reportedViewedPatientToAnalytics()

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
}
