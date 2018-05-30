package org.resolvetosavelives.red.sync

import com.f2prateek.rx.preferences2.Preference
import com.gojuno.koptional.Optional
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.newentry.search.SyncStatus
import org.threeten.bp.Instant
import java.util.Collections

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
    whenever(repository.patientsWithSyncStatus(SyncStatus.PENDING)).thenReturn(Single.just(Collections.emptyList()))

    patientSync.push().blockingAwait()

    verify(api, never()).push(any())
  }

  @Test
  fun `when a push succeeds then any patients that were created after the push started should not get marked as synced`() {
    // TODO.
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
