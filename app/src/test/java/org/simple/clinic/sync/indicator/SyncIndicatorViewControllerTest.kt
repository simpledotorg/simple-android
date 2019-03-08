package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.communication.CommunicationRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.indicator.SyncIndicatorState.ConnectToSync
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(JUnitParamsRunner::class)
class SyncIndicatorViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val lastSyncStatePreference = mock<Preference<LastSyncedState>>()

  lateinit var controller: SyncIndicatorViewController
  private val lastSyncStateStream = PublishSubject.create<LastSyncedState>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val indicator = mock<SyncIndicatorView>()

  private val utcClock = TestUtcClock()

  private val dataSync = mock<DataSync>()

  private val configSubject = PublishSubject.create<SyncIndicatorConfig>()

  private val patientRepository = mock<PatientRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val communicationRepository = mock<CommunicationRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val bpRepository = mock<BloodPressureRepository>()

  @Before
  fun setUp() {
    controller = SyncIndicatorViewController(
        lastSyncState = lastSyncStatePreference,
        utcClock = utcClock,
        configProvider = configSubject,
        dataSync = dataSync,
        patientRepository = patientRepository,
        bloodPressureRepository = bpRepository,
        prescriptionRepository = prescriptionRepository,
        appointmentRepository = appointmentRepository,
        communicationRepository = communicationRepository,
        medicalHistoryRepository = medicalHistoryRepository
    )
    whenever(lastSyncStatePreference.asObservable()).thenReturn(lastSyncStateStream)
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.never())
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.never())
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.never())
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.never())
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.never())
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.never())

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(indicator) }
  }

  @Test
  fun `when sync progress is not set, sync status indicator should show sync pending`() {
    uiEvents.onNext(SyncIndicatorViewCreated)
    lastSyncStateStream.onNext(LastSyncedState())

    verify(indicator).updateState(SyncPending)
  }

  @Test
  @Parameters(method = "params for testing sync status indicator update")
  fun `when sync result is updated, sync status indicator should change`(
      lastSyncState: LastSyncedState,
      expectedSyncState: SyncIndicatorState,
      syncFailureThreshold: Long
  ) {
    uiEvents.onNext(SyncIndicatorViewCreated)
    val config = SyncIndicatorConfig(Duration.of(syncFailureThreshold, ChronoUnit.HOURS))

    configSubject.onNext(config)
    lastSyncStateStream.onNext(lastSyncState)

    verify(indicator).updateState(expectedSyncState)
  }

  @Suppress("Unused")
  private fun `params for testing sync status indicator update`(): List<List<Any>> {
    return listOf(
        listOf(LastSyncedState(SYNCING), Syncing, 12),
        listOf(LastSyncedState(FAILURE), SyncPending, 12),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock)), Synced(durationSince = Duration.ZERO), 12),
        listOf(LastSyncedState(FAILURE, Instant.now(utcClock).minus(13, ChronoUnit.HOURS)), ConnectToSync, 11),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock).minus(20, ChronoUnit.MINUTES)), SyncPending, 11),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock).minus(14, ChronoUnit.HOURS)), ConnectToSync, 13),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock).minus(12, ChronoUnit.MINUTES)), Synced(durationSince = Duration.ofMinutes(12)), 12),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock).plus(12, ChronoUnit.MINUTES)), SyncPending, 12),
        listOf(LastSyncedState(FAILURE, Instant.now(utcClock).plus(12, ChronoUnit.MINUTES)), SyncPending, 13),
        listOf(LastSyncedState(FAILURE, Instant.now(utcClock).minus(12, ChronoUnit.MINUTES)), SyncPending, 12)
    )
  }

  @Test
  fun `when sync indicator is clicked, sync should be triggered`() {
    whenever(dataSync.sync(SyncGroup.FREQUENT)).thenReturn(Completable.complete())
    whenever(dataSync.streamSyncErrors()).thenReturn(Observable.never())

    lastSyncStateStream.onNext(LastSyncedState())
    uiEvents.onNext(SyncIndicatorViewClicked)

    verify(dataSync).sync(SyncGroup.FREQUENT)
  }

  @Test
  @Parameters(method = "params for testing sync errors")
  fun `when sync indicator is clicked and sync starts, appropriate failure dialog should show if any sync error is thrown`(error: ResolvedError) {
    whenever(dataSync.streamSyncErrors()).thenReturn(Observable.just(error))
    whenever(dataSync.sync(SyncGroup.FREQUENT)).thenReturn(Completable.complete())

    lastSyncStateStream.onNext(LastSyncedState(lastSyncProgress = SUCCESS))
    uiEvents.onNext(SyncIndicatorViewClicked)

    verify(dataSync).sync(SyncGroup.FREQUENT)
    verify(dataSync).streamSyncErrors()
    verify(indicator).showErrorDialog(errorType = error)
  }

  @Suppress("unused")
  private fun `params for testing sync errors`(): List<Any> {
    return listOf(
        ResolvedError.NetworkRelated(UnknownHostException()),
        ResolvedError.NetworkRelated(SocketTimeoutException()),
        ResolvedError.Unexpected(RuntimeException())
    )
  }

  @Test
  fun `when last sync state is syncing then we should not trigger a manual sync on click`() {
    lastSyncStateStream.onNext(LastSyncedState(lastSyncProgress = SYNCING))
    uiEvents.onNext(SyncIndicatorViewClicked)

    verify(dataSync, never()).sync(any())
    verify(dataSync, never()).streamSyncErrors()
    verify(indicator, never()).showErrorDialog(any())
  }

  @Test
  fun `if records are pending sync, then sync indicator should show Sync Pending`() {
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.just(1))
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.just(2))
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.just(5))
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.just(3))
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.just(1))

    uiEvents.onNext(SyncIndicatorViewCreated)

    verify(indicator).updateState(SyncPending)
  }

  @Test
  fun `if patient record is pending sync, then sync indicator should show Sync Pending`() {
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.just(1))
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.just(0))

    uiEvents.onNext(SyncIndicatorViewCreated)

    verify(indicator).updateState(SyncPending)
  }

  @Test
  fun `if bp record is pending sync, then sync indicator should show Sync Pending`() {
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.just(1))
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.just(0))

    uiEvents.onNext(SyncIndicatorViewCreated)

    verify(indicator).updateState(SyncPending)
  }

  @Test
  fun `if appointments are pending sync, then sync indicator should show Sync Pending`() {
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.just(3))
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.just(0))

    uiEvents.onNext(SyncIndicatorViewCreated)

    verify(indicator).updateState(SyncPending)
  }

  @Test
  fun `if communications are pending sync, then sync indicator should show Sync Pending`() {
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.just(2))

    uiEvents.onNext(SyncIndicatorViewCreated)

    verify(indicator).updateState(SyncPending)
  }

  @Test
  fun `if prescriptions are pending sync, then sync indicator should show Sync Pending`() {
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.just(2))
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.just(0))

    uiEvents.onNext(SyncIndicatorViewCreated)

    verify(indicator).updateState(SyncPending)
  }

  @Test
  fun `if medical histories are pending sync, then sync indicator should show Sync Pending`() {
    whenever(patientRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(bpRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(prescriptionRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(medicalHistoryRepository.pendingRecordsCount()).thenReturn(Observable.just(2))
    whenever(appointmentRepository.pendingRecordsCount()).thenReturn(Observable.just(0))
    whenever(communicationRepository.pendingRecordsCount()).thenReturn(Observable.just(0))

    uiEvents.onNext(SyncIndicatorViewCreated)

    verify(indicator).updateState(SyncPending)
  }
}
