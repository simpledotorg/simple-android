package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class PatientSummaryScreenControllerTest {

  private val screen = mock<PatientSummaryScreen>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: PatientSummaryScreenController

  @Before
  fun setUp() {
    val timestampGenerator = mock<RelativeTimestampGenerator>()
    whenever(timestampGenerator.generate(any())).thenReturn(Today())

    controller = PatientSummaryScreenController(patientRepository, bpRepository, timestampGenerator)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is opened then patient details should be populated`() {
    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()
    val patient = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid)
    val address = PatientMocker.address(uuid = addressUuid)
    val phoneNumber = None

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.just(Just(patient)))
    whenever(patientRepository.address(addressUuid)).thenReturn(Observable.just(Just(address)))
    whenever(patientRepository.phoneNumbers(patientUuid)).thenReturn(Observable.just(phoneNumber))

    val bloodPressureMeasurements = listOf(
        PatientMocker.bp(patientUuid, systolic = 120, diastolic = 85),
        PatientMocker.bp(patientUuid, systolic = 164, diastolic = 95),
        PatientMocker.bp(patientUuid, systolic = 144, diastolic = 90))
    whenever(bpRepository.recentMeasurementsForPatient(patientUuid)).thenReturn(Observable.just(bloodPressureMeasurements))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT))

    verify(screen).populatePatientInfo(patient, address, phoneNumber)
    verify(screen).setupSummaryList()
    verify(screen).populateSummaryList(check {
      it.forEachIndexed { i, item -> assertThat(item.measurement == bloodPressureMeasurements[i]) }
    })
  }

  @Test
  fun `when new-BP is clicked then BP entry sheet should be shown`() {
    whenever(patientRepository.patient(any())).thenReturn(Observable.never())
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.never())
    whenever(bpRepository.recentMeasurementsForPatient(any())).thenReturn(Observable.never())

    val patientUuid = UUID.randomUUID()
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH))
    uiEvents.onNext(PatientSummaryNewBpClicked())

    verify(screen).showBloodPressureEntrySheet(patientUuid)
  }

  @Test
  fun `when screen was opened after saving a new patient then BP entry sheet should be shown`() {
    whenever(patientRepository.patient(any())).thenReturn(Observable.never())
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.never())
    whenever(bpRepository.recentMeasurementsForPatient(any())).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.SEARCH))
    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.NEW_PATIENT))

    verify(screen, times(1)).showBloodPressureEntrySheet(any())
  }

  @Test
  fun `when screen was opened from search and up button is pressed then the user should be taken back to search`() {
    whenever(patientRepository.patient(any())).thenReturn(Observable.never())
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.never())
    whenever(bpRepository.recentMeasurementsForPatient(any())).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.NEW_PATIENT))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen).goBackToHome()
  }

  @Test
  fun `when screen was opened after saving a new patient and up button is pressed then the user should be taken back to home`() {
    whenever(patientRepository.patient(any())).thenReturn(Observable.never())
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.never())
    whenever(bpRepository.recentMeasurementsForPatient(any())).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.SEARCH))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen).goBackToPatientSearch()
  }

  @Test
  fun `when screen is opened after saving a new patient then done button should be shown`() {
    // TODO.
  }

  @Test
  fun `when update medicines is clicked then BP medicines screen should be shown`() {
    whenever(patientRepository.patient(any())).thenReturn(Observable.never())
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.never())
    whenever(bpRepository.recentMeasurementsForPatient(any())).thenReturn(Observable.never())

    val patientUuid = UUID.randomUUID()
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH))
    uiEvents.onNext(PatientSummaryUpdateDrugsClicked())

    verify(screen).showUpdatePrescribedDrugsScreen(patientUuid)
  }
}
