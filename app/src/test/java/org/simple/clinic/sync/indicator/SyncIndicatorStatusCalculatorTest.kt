package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.DataSync.SyncGroupResult
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncTag.DAILY
import org.simple.clinic.sync.SyncTag.FREQUENT
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
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
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(FREQUENT, SUCCESS)))

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
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(FREQUENT, FAILURE)))

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
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(FREQUENT, SYNCING)))

    // when
    syncCalculator.updateSyncResults()

    // then
    verify(syncResultPreference).set(LastSyncedState(SYNCING, lastSyncSucceededAt))
  }

  @Test
  @Parameters(value = ["SUCCESS", "FAILURE", "SYNCING"])
  fun `when the daily sync group sync status changes, the last synced state preference should not be affected`(progress: SyncProgress) {
    // given
    val lastSyncSucceededAt = Instant.now(clock).minusSeconds(1)
    val initialState = LastSyncedState(lastSyncProgress = SUCCESS, lastSyncSucceededAt = lastSyncSucceededAt)
    whenever(syncResultPreference.get()).thenReturn(initialState)
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(DAILY, progress)))

    // when
    syncCalculator.updateSyncResults()

    // then
    verify(syncResultPreference, never()).set(any())
  }
}
