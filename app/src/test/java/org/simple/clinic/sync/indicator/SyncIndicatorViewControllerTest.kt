package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncGroup.FREQUENT
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.sync.indicator.SyncIndicatorState.ConnectToSync
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.ServerError
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.ResolvedError.Unexpected
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.net.UnknownHostException

@RunWith(JUnitParamsRunner::class)
class SyncIndicatorViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val lastSyncStatePreference = mock<Preference<LastSyncedState>>()
  private val indicator = mock<SyncIndicatorView>()
  private val dataSync = mock<DataSync>()
  private val utcClock = TestUtcClock()
  private val frequentlySyncingRepositories = arrayListOf<SynceableRepository<*, *>>()
  private val lastSyncStateStream = PublishSubject.create<LastSyncedState>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val configSubject = PublishSubject.create<SyncIndicatorConfig>()

  @Before
  fun setUp() {
    whenever(lastSyncStatePreference.asObservable()).thenReturn(lastSyncStateStream)
  }

  @Test
  fun `when sync progress is not set, sync status indicator should show sync pending`() {
    //when
    setupController()
    uiEvents.onNext(SyncIndicatorViewCreated)
    lastSyncStateStream.onNext(LastSyncedState())

    //then
    verify(indicator).updateState(SyncPending)
  }

  @Test
  @Parameters(method = "params for testing sync status indicator update")
  fun `when sync result is updated, sync status indicator should change`(
      lastSyncState: LastSyncedState,
      expectedSyncState: SyncIndicatorState,
      syncFailureThreshold: Long
  ) {
    //given
    val config = SyncIndicatorConfig(Duration.of(syncFailureThreshold, ChronoUnit.HOURS))

    //when
    setupController()
    uiEvents.onNext(SyncIndicatorViewCreated)
    configSubject.onNext(config)
    lastSyncStateStream.onNext(lastSyncState)

    //then
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
    //given
    whenever(dataSync.streamSyncErrors()).thenReturn(Observable.never())

    //when
    setupController()
    lastSyncStateStream.onNext(LastSyncedState())
    uiEvents.onNext(SyncIndicatorViewClicked)

    //then
    verify(dataSync).fireAndForgetSync(FREQUENT)
  }

  data class ShowFailureDialogParams(
      val error: ResolvedError,
      val shouldShowErrorDialog: Boolean
  )

  @Test
  @Parameters(method = "params for showing failure dialog")
  fun `appropriate failure dialog should be shown for all sync errors except Unauthorized`(
      testCase: ShowFailureDialogParams
  ) {
    // given
    val (error: ResolvedError,
        shouldShowErrorDialog: Boolean) = testCase
    whenever(dataSync.streamSyncErrors()).thenReturn(Observable.just(error))

    // when
    setupController()
    lastSyncStateStream.onNext(LastSyncedState())
    uiEvents.onNext(SyncIndicatorViewClicked)

    // then
    if (shouldShowErrorDialog) {
      verify(indicator).showErrorDialog(error)
    } else {
      verify(indicator, never()).showErrorDialog(any())
    }
  }

  @Suppress("Unused")
  private fun `params for showing failure dialog`(): List<ShowFailureDialogParams> {
    return listOf(
        ShowFailureDialogParams(NetworkRelated(UnknownHostException()), true),
        ShowFailureDialogParams(NetworkRelated(UnknownHostException()), true),
        ShowFailureDialogParams(Unexpected(RuntimeException()), true),
        ShowFailureDialogParams(ServerError(RuntimeException()), true),
        ShowFailureDialogParams(Unauthenticated(RuntimeException()), false)
    )
  }

  @Test
  fun `when last sync state is syncing then we should not trigger a manual sync on click`() {
    //when
    setupController()
    lastSyncStateStream.onNext(LastSyncedState(lastSyncProgress = SYNCING))
    uiEvents.onNext(SyncIndicatorViewClicked)

    //then
    verifyZeroInteractions(dataSync)
    verifyZeroInteractions(indicator)
  }

  @Test
  fun `if pending sync are present, then sync indicator should show Sync Pending`() {
    //given
    val patientRepository = mock<PatientRepository>()
    whenever(patientRepository.pendingSyncRecordCount()).thenReturn(Observable.just(1))
    frequentlySyncingRepositories.add(patientRepository)

    val appointmentRepository = mock<AppointmentRepository>()
    whenever(appointmentRepository.pendingSyncRecordCount()).thenReturn(Observable.just(0))
    frequentlySyncingRepositories.add(appointmentRepository)

    //when
    setupController()
    uiEvents.onNext(SyncIndicatorViewCreated)

    //then
    verify(indicator).updateState(SyncPending)
  }

  fun setupController() {
    val controller = SyncIndicatorViewController(
        lastSyncState = lastSyncStatePreference,
        utcClock = utcClock,
        configProvider = configSubject,
        dataSync = dataSync,
        frequentlySyncingRepositories = frequentlySyncingRepositories
    )

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(indicator) }
  }
}
