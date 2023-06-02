package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.DataSync.SyncGroupResult
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.time.Instant

@RunWith(JUnitParamsRunner::class)
class SyncIndicatorStatusCalculatorTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val dataSync = mock<DataSync>()
  private val syncResultPreference = mock<Preference<LastSyncedState>>()
  private val clock = TestUtcClock()
  private val schedulersProvider = TrampolineSchedulersProvider()

  private val syncCalculator = SyncIndicatorStatusCalculator(dataSync, clock, syncResultPreference, schedulersProvider)

  @Test
  fun `when the frequent sync group is synced successfully, the last synced state preference should be set to success with the current timestamp`() {
    // given
    val initialState = LastSyncedState()
    whenever(syncResultPreference.get()).thenReturn(initialState)
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(SUCCESS)))

    // when
    syncCalculator.updateSyncResults()

    // then
    verify(syncResultPreference).set(LastSyncedState(SUCCESS, Instant.now(clock)))
  }

  @Test
  fun `when the frequent sync group fails to sync, the last synced state preference should be set to failure without changing the sync timestamp`() {
    // given
    val lastSyncSucceededAt = Instant.now(clock).minusSeconds(1)
    val initialState = LastSyncedState(lastSyncProgress = SUCCESS, lastSyncSucceededAt = lastSyncSucceededAt)
    whenever(syncResultPreference.get()).thenReturn(initialState)
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(FAILURE)))

    // when
    syncCalculator.updateSyncResults()

    // then
    verify(syncResultPreference).set(LastSyncedState(FAILURE, lastSyncSucceededAt))
  }

  @Test
  fun `when the frequent sync group begins to sync, the last synced state preference should be set to syncing without changing the sync timestamp`() {
    // given
    val lastSyncSucceededAt = Instant.now(clock).minusSeconds(1)
    val initialState = LastSyncedState(lastSyncProgress = SUCCESS, lastSyncSucceededAt = lastSyncSucceededAt)
    whenever(syncResultPreference.get()).thenReturn(initialState)
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(SYNCING)))

    // when
    syncCalculator.updateSyncResults()

    // then
    verify(syncResultPreference).set(LastSyncedState(SYNCING, lastSyncSucceededAt))
  }
}
