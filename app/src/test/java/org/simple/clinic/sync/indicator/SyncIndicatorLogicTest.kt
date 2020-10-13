package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncTag.FREQUENT
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
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.net.UnknownHostException

@RunWith(JUnitParamsRunner::class)
class SyncIndicatorLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val lastSyncStatePreference = mock<Preference<LastSyncedState>>()
  private val indicatorUiActions = mock<SyncIndicatorUiActions>()
  private val indicator = mock<SyncIndicatorUi>()
  private val dataSync = mock<DataSync>()
  private val utcClock = TestUtcClock()
  private val frequentlySyncingRepositories = arrayListOf<SynceableRepository<*, *>>()
  private val lastSyncStateStream = PublishSubject.create<LastSyncedState>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val defaultLastSyncedState = LastSyncedState()
  lateinit var testFixture: MobiusTestFixture<SyncIndicatorModel, SyncIndicatorEvent, SyncIndicatorEffect>

  @Before
  fun setUp() {
    whenever(lastSyncStatePreference.asObservable()).thenReturn(lastSyncStateStream)
  }

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when sync progress is not set, sync status indicator should show sync pending`() {
    //when
    startMobiusLoop()
    lastSyncStateStream.onNext(defaultLastSyncedState)

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
    startMobiusLoop(config)
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
    startMobiusLoop()
    lastSyncStateStream.onNext(defaultLastSyncedState)
    uiEvents.onNext(SyncIndicatorViewClicked)

    //then
    verify(dataSync).fireAndForgetSync(FREQUENT)
  }

  @Test
  @Parameters(method = "params for showing failure dialog")
  fun `failure dialog should be shown for all types of sync errors except unauthenticated`(error: ResolvedError) {
    // given
    whenever(dataSync.streamSyncErrors()).thenReturn(Observable.just(error))

    // when
    startMobiusLoop()
    lastSyncStateStream.onNext(defaultLastSyncedState)
    uiEvents.onNext(SyncIndicatorViewClicked)

    // then
    verify(indicatorUiActions).showErrorDialog(error)
  }

  @Suppress("Unused")
  private fun `params for showing failure dialog`(): List<ResolvedError> {
    return listOf(
        NetworkRelated(UnknownHostException()),
        NetworkRelated(UnknownHostException()),
        Unexpected(RuntimeException()),
        ServerError(RuntimeException())
    )
  }

  @Test
  fun `failure dialog should not be shown for unauthenticated as sync error`() {
    // given
    whenever(dataSync.streamSyncErrors()).thenReturn(Observable.just(Unauthenticated(RuntimeException())))

    // when
    startMobiusLoop()
    lastSyncStateStream.onNext(defaultLastSyncedState)
    uiEvents.onNext(SyncIndicatorViewClicked)

    // then
    verify(indicatorUiActions, never()).showErrorDialog(any())
  }

  @Test
  fun `when last sync state is syncing then we should not trigger a manual sync on click`() {
    //given
    val syncingState = LastSyncedState(lastSyncProgress = SYNCING)

    //when
    startMobiusLoop()
    lastSyncStateStream.onNext(syncingState)
    uiEvents.onNext(SyncIndicatorViewClicked)

    //then
    verify(indicator).updateState(Syncing)
    verifyZeroInteractions(dataSync)
    verifyNoMoreInteractions(indicator)
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
    startMobiusLoop()

    //then
    verify(indicator).updateState(SyncPending)
  }

  private fun startMobiusLoop(config: SyncIndicatorConfig = SyncIndicatorConfig(Duration.of(12, ChronoUnit.HOURS))) {
    val uiRenderer = SyncIndicatorUiRenderer(indicator)
    val effectHandler = SyncIndicatorEffectHandler(
        lastSyncStatePreference,
        utcClock,
        config,
        TrampolineSchedulersProvider(),
        dataSync,
        frequentlySyncingRepositories,
        indicatorUiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = SyncIndicatorModel.create(),
        init = SyncIndicatorInit(),
        update = SyncIndicatorUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
