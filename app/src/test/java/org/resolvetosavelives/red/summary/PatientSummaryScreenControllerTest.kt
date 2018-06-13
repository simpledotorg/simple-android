package org.resolvetosavelives.red.summary

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.patient.PatientAddress
import org.resolvetosavelives.red.patient.PatientFaker
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.widgets.UiEvent
import java.util.UUID

class PatientSummaryScreenControllerTest {

  private val screen = mock<PatientSummaryScreen>()
  private val repository = mock<PatientRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: PatientSummaryScreenController

  @Before
  fun setUp() {
    controller = PatientSummaryScreenController(repository)

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(screen) })
  }

  @Test
  fun `when screen is opened then patient details should be set on UI`() {
    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val patient = PatientFaker.patient(uuid = patientUuid, addressUuid = addressUuid)

    val address = PatientAddress(
        uuid = addressUuid,
        colonyOrVillage = "colony/village",
        district = "district",
        state = "state",
        country = "India",
        createdAt = mock(),
        updatedAt = mock())

    val phoneNumber = None

    whenever(repository.patient(patientUuid)).thenReturn(Observable.just(Just(patient)))
    whenever(repository.address(addressUuid)).thenReturn(Observable.just(Just(address)))
    whenever(repository.phoneNumbers(patientUuid)).thenReturn(Observable.just(phoneNumber))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT))

    verify(screen).preFill(patient, address, phoneNumber)
  }

  @Test
  fun `when screen was opened after saving a new patient then BP entry sheet should be shown`() {
    whenever(repository.patient(any())).thenReturn(Observable.never())
    whenever(repository.phoneNumbers(any())).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.SEARCH))
    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.NEW_PATIENT))

    verify(screen, times(1)).showBloodPressureEntrySheet(any())
  }

  @Test
  fun `when screen was opened from search and up button is pressed then the user should be taken back to search`() {
    whenever(repository.patient(any())).thenReturn(Observable.never())
    whenever(repository.phoneNumbers(any())).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.NEW_PATIENT))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen).goBackToHome()
  }

  @Test
  fun `when screen was opened after saving a new patient and up button is pressed then the user should be taken back to home`() {
    whenever(repository.patient(any())).thenReturn(Observable.never())
    whenever(repository.phoneNumbers(any())).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(UUID.randomUUID(), caller = PatientSummaryCaller.SEARCH))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen).goBackToPatientSearch()
  }
}
