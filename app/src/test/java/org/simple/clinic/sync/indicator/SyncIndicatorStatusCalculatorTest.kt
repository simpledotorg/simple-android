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
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncGroupResult
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset

@RunWith(JUnitParamsRunner::class)
class SyncIndicatorStatusCalculatorTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val dataSync = mock<DataSync>()

  lateinit var syncCalculator: SyncIndicatorStatusCalculator

  private val syncTimestampPreference = mock<Preference<Optional<Instant>>>()
  private val syncResultPreference = mock<Preference<Optional<SyncGroupResult>>>()
  private val clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)

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
  fun `when a frequent sync group is synced successfully, the preferences should be set`(syncGroup: SyncGroup, syncGroupResult: SyncGroupResult) {
    whenever(dataSync.streamSyncResults()).thenReturn(Observable.just(Pair(syncGroup, syncGroupResult)))
    syncCalculator = SyncIndicatorStatusCalculator(dataSync, clock, syncTimestampPreference, syncResultPreference)

    when (syncGroup) {
      SyncGroup.FREQUENT -> {
        verify(syncResultPreference).set(Just(syncGroupResult))
        if (syncGroupResult == SyncGroupResult.SUCCESS) {
          verify(syncTimestampPreference).set(Just(Instant.now(clock)))
        } else {
          verify(syncTimestampPreference, never()).set(any())
        }
      }
      SyncGroup.DAILY -> {
        verify(syncResultPreference, never()).set(any())
        verify(syncTimestampPreference, never()).set(any())
      }
    }
  }


}
