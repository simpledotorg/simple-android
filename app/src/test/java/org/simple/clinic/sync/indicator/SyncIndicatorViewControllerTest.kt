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
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant

@RunWith(JUnitParamsRunner::class)
class SyncIndicatorViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val syncTimestampPreference = mock<Preference<Optional<Instant>>>()
  private val syncResultPreference = mock<Preference<Optional<SyncProgress>>>()

  lateinit var controller: SyncIndicatorViewController
  private val syncProgressStream = PublishSubject.create<Optional<SyncProgress>>()
  private val syncTimestampStream = PublishSubject.create<Optional<Instant>>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val indicator = mock<SyncIndicatorView>()

  @Before
  fun setUp() {
    controller = SyncIndicatorViewController(syncTimestampPreference, syncResultPreference)
    whenever(syncResultPreference.asObservable()).thenReturn(syncProgressStream)
    whenever(syncTimestampPreference.asObservable()).thenReturn(syncTimestampStream)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(indicator) }
  }

  @Test
  @Parameters(method = "params for testing sync status indicator update")
  fun `when sync result is updated, sync status indicator should change`(syncProgress: SyncProgress, expectedSyncState: SyncIndicatorState) {
    uiEvents.onNext(SyncIndicatorViewCreated)
    syncProgressStream.onNext(Just(syncProgress))

    verify(indicator).updateState(expectedSyncState)
  }

  @Suppress("Unused")
  private fun `params for testing sync status indicator update`(): List<List<Any>> {
    return listOf(
        listOf(SYNCING, Syncing),
        listOf(SUCCESS, Synced),
        listOf(FAILURE, SyncPending)
    )
  }
}
