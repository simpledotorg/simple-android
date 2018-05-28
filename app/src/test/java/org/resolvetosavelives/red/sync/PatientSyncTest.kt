package org.resolvetosavelives.red.sync

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.PatientRepository

class PatientSyncTest {

  private val api: PatientSyncApiV1 = mock()
  private val repository: PatientRepository = mock()
  private val firstSyncDone = mock<Preference<Boolean>>()
  private lateinit var patientSync: PatientSync

  @Before
  fun setup() {
    patientSync = PatientSync(api, repository, firstSyncDone)
  }

  @Test
  fun `when pending-sync patients are empty then the server should not get called`() {
    whenever(repository.pendingSyncPatients()).thenReturn(Single.just(listOf()))

    patientSync.sync()

    verify(api, never()).push(any())
  }

  @Test
  fun `when a push succeeds then any patients that were created after the push started should not get marked as synced`() {
    // TODO.
  }

  @Test
  fun `when pulling patients for the time then 'first_time' query-param should be set`() {

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
