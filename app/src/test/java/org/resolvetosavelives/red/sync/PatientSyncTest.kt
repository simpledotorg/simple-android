package org.resolvetosavelives.red.sync

import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.PatientRepository

class PatientSyncTest {

  private val api: PatientSyncApi = mock()
  private val repository: PatientRepository = mock()

  private lateinit var patientSync: PatientSync

  @Before
  fun setup() {
    patientSync = PatientSync(api, repository)
  }

  @Test
  fun `when pending-sync patients are present then they should be pushed to the server`() {

  }

  @Test
  fun `when pending-sync patients are empty then the server should not get called`() {

  }

  @Test
  fun `when patients have been pushed then mark them as synced`() {

  }

  @Test
  fun `when a push succeeds then any patients that were created after the push started should not get marked as synced`() {
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
  fun `errors during syncing should not get swallowed`() {
    // Because syncing can be re-scheduled for later if the error is recoverable.
  }

  @Test
  fun `errors during push or pull should not affect each other`() {
  }
}
