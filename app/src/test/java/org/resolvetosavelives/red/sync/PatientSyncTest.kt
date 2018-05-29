package org.resolvetosavelives.red.sync

import com.f2prateek.rx.preferences2.Preference
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.threeten.bp.Instant

class PatientSyncTest {

  private val api: PatientSyncApiV1 = mock()
  private val repository: PatientRepository = mock()
  private val lastSyncTimestamp: Preference<Optional<Instant>> = mock()

  private lateinit var syncConfigProvider: () -> PatientSyncConfig

  private lateinit var patientSync: PatientSync

  @Before
  fun setup() {
    // Do not move this to property initialization. It's important to reset this on every test.
    syncConfigProvider = mock()
    patientSync = PatientSync(api, repository, Single.fromCallable { syncConfigProvider() }, lastSyncTimestamp)
  }

  @Test
  fun `when pending-sync patients are empty then the server should not get called`() {
    whenever(repository.pendingSyncPatients()).thenReturn(Single.just(listOf()))

    patientSync.push().blockingAwait()

    verify(api, never()).push(any())
  }

  @Test
  fun `when a push succeeds then any patients that were created after the push started should not get marked as synced`() {
    // TODO.
  }

  @Test
  fun `when pulling patients for the time then 'first_time' query-param should be set`() {
    val config = PatientSyncConfig(frequency = mock(), batchSize = 10)
    whenever(lastSyncTimestamp.asObservable()).thenReturn(Observable.just(None))
    whenever(syncConfigProvider()).thenReturn(config)
    whenever(api.pull(isFirstSync = true, recordsToRetrieve = config.batchSize)).thenReturn(Single.just(PatientPullResponse(listOf(), Instant.now())))
    whenever(repository.mergeWithLocalDatabase(any())).thenReturn(Completable.complete())

    patientSync.pull().blockingAwait()

    verify(api).pull(isFirstSync = true, recordsToRetrieve = config.batchSize)
  }

  @Test
  fun `when pulling patients for subsequent times then a pagination-anchor should be set`() {

  }

  @Test
  fun `when pulling patients then pagination should continue if the server has more patients`() {

  }

  @Test
  fun `when pulling patients then pagination should end if the server does not have anymore patients`() {

  }

  @Test
  fun `when patients are received then pagination anchor should be saved`() {

  }

  @Test
  fun `when saving received patients and newer patient records are present in local storage then stale received patients should be ignored`() {

  }

  @Test
  fun `unexpected errors during syncing should not get swallowed`() {
    // Because syncing can be re-scheduled for later if the error is recoverable.
  }

  @Test
  fun `errors during push or pull should not affect each other`() {
  }
}
