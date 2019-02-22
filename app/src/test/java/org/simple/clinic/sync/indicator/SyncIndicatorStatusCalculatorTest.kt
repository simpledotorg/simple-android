package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.DataSync.SyncGroupResult
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Instant

@RunWith(JUnitParamsRunner::class)
class SyncIndicatorStatusCalculatorTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val dataSync = mock<DataSync>()

  lateinit var syncCalculator: SyncIndicatorStatusCalculator

  private val syncResultPreference = mock<Preference<LastSyncedState>>()
  private val clock = TestUtcClock()

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
  }

  @Test
  @Parameters(value = [
    "FREQUENT|SUCCESS",
    "DAILY|SUCCESS",
    "FREQUENT|FAILURE",
    "DAILY|FAILURE",
    "FREQUENT|SYNCING"
  ])
  fun `when the frequent sync group is synced successfully, the last synced state preference should be set`(
      syncGroup: SyncGroup,
      syncProgress: SyncProgress
  ) {
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(SyncGroupResult(syncGroup, syncProgress)))

    val initialState = LastSyncedState()
    whenever(syncResultPreference.get()).thenReturn(initialState)

    syncCalculator = SyncIndicatorStatusCalculator(dataSync, clock, syncResultPreference)

    when (syncGroup) {
      SyncGroup.FREQUENT -> {
        when (syncProgress) {
          SUCCESS -> verify(syncResultPreference).set(LastSyncedState(syncProgress, Instant.now(clock)))
          FAILURE, SYNCING -> verify(syncResultPreference).set(LastSyncedState(syncProgress, initialState.lastSyncSucceededAt))
        }
      }
      SyncGroup.DAILY -> {
        verify(syncResultPreference, never()).set(any())
      }
    }
  }
}
