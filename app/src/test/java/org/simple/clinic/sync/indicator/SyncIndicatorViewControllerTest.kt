package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.indicator.SyncIndicatorState.ConnectToSync
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

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

  @Before
  fun setUp() {
    controller = SyncIndicatorViewController(lastSyncStatePreference, utcClock)
    whenever(lastSyncStatePreference.asObservable()).thenReturn(lastSyncStateStream)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(indicator) }
  }

  @Test
  @Parameters(method = "params for testing sync status indicator update")
  fun `when sync result is updated, sync status indicator should change`(lastSyncState: LastSyncedState, expectedSyncState: SyncIndicatorState) {
    uiEvents.onNext(SyncIndicatorViewCreated)
    lastSyncStateStream.onNext(lastSyncState)

    verify(indicator).updateState(expectedSyncState)
  }

  @Suppress("Unused")
  private fun `params for testing sync status indicator update`(): List<List<Any>> {
    return listOf(
        listOf(LastSyncedState(SYNCING), Syncing),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock)), Synced),
        listOf(LastSyncedState(FAILURE), SyncPending),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock).minus(20, ChronoUnit.MINUTES)), SyncPending),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock).minus(13, ChronoUnit.HOURS)), ConnectToSync),
        listOf(LastSyncedState(SUCCESS, Instant.now(utcClock).minus(12, ChronoUnit.MINUTES)), Synced)
    )
  }
}
