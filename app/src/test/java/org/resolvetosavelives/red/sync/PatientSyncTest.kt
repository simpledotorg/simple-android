package org.resolvetosavelives.red.sync

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.reset
import org.resolvetosavelives.red.search.PatientAddress
import org.resolvetosavelives.red.search.PatientRepository
import org.resolvetosavelives.red.search.PatientWithAddressAndPhone
import org.resolvetosavelives.red.search.SyncStatus.DONE
import org.resolvetosavelives.red.search.SyncStatus.INVALID
import org.resolvetosavelives.red.search.SyncStatus.IN_FLIGHT
import org.resolvetosavelives.red.search.SyncStatus.PENDING
import org.resolvetosavelives.red.sync.patient.DataPushResponse
import org.resolvetosavelives.red.sync.patient.PatientPullResponse
import org.resolvetosavelives.red.sync.patient.PatientSync
import org.resolvetosavelives.red.sync.patient.PatientSyncApiV1
import org.resolvetosavelives.red.sync.patient.ValidationErrors
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.util.Optional
import org.threeten.bp.Instant
import java.util.Collections
import java.util.UUID

class PatientSyncTest {

  private val api: PatientSyncApiV1 = mock()
  private val repository: PatientRepository = mock()
  private val lastSyncTimestamp: Preference<Optional<Instant>> = mock()

  // This is a lambda just so that the config can be changed later.
  private val syncConfigProvider: () -> SyncConfig = mock()

  private lateinit var patientSync: PatientSync

  @Before
  fun setup() {
    patientSync = PatientSync(api, repository, Single.fromCallable { syncConfigProvider() }, lastSyncTimestamp)
  }

  @Test
  fun `when pending-sync patients are empty then the server should not get called`() {
    whenever(repository.patientsWithSyncStatus(PENDING)).thenReturn(Single.just(Collections.emptyList()))

    patientSync.push().blockingAwait()

    verify(api, never()).push(any())
  }

  @Test
  fun `when a push succeeds then any patients that were created or updated during the push started should not get marked as synced`() {
    // TODO: I'm out of ideas to write this test because it involves concurrency.
  }

  @Test
  fun `errors during push should not affect pull`() {
    whenever(repository.patientsWithSyncStatus(any())).thenReturn(Single.error(NullPointerException()))

    val config = SyncConfig(mock(), batchSize = 10)
    whenever(syncConfigProvider()).thenReturn(config)
    whenever(lastSyncTimestamp.asObservable()).thenReturn(Observable.just(None))
    whenever(api.pull(config.batchSize, isFirstPull = true)).thenReturn(Single.just(PatientPullResponse(mock(), mock())))
    whenever(repository.mergeWithLocalData(any())).thenReturn(Completable.complete())

    patientSync.sync().test()
        .await()
        .assertError(NullPointerException::class.java)

    verify(api, never()).push(any())
    verify(api).pull(config.batchSize, isFirstPull = true)
  }

  @Test
  fun `errors during pull should not affect push`() {
    whenever(repository.patientsWithSyncStatus(PENDING)).thenReturn(Single.just(listOf(mock())))
    whenever(repository.updatePatientsSyncStatus(oldStatus = PENDING, newStatus = IN_FLIGHT)).thenReturn(Completable.complete())
    whenever(api.push(any())).thenReturn(Single.just(DataPushResponse(listOf())))
    whenever(repository.updatePatientsSyncStatus(oldStatus = IN_FLIGHT, newStatus = DONE)).thenReturn(Completable.complete())

    whenever(syncConfigProvider()).thenThrow(AssertionError())

    patientSync.sync()
        .test()
        .await()
        .assertError(AssertionError::class.java)

    verify(api, never()).pull(recordsToPull = any(), isFirstPull = any())
    verify(api).push(any())
  }

  @Test
  fun `if there are validation errors during push, then the flagged patient should be marked as invalid`() {
    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()
    val patientAddress = PatientAddress(addressUuid, "colony", "district", "state", "country", mock(), mock())
    val patientWithErrors = PatientWithAddressAndPhone(
        uuid = patientUuid,
        gender = mock(),
        dateOfBirth = mock(),
        fullName = "name",
        age = mock(),
        status = mock(),
        createdAt = mock(),
        updatedAt = mock(),
        syncStatus = mock(),
        address = patientAddress)

    whenever(repository.patientsWithSyncStatus(PENDING)).thenReturn(Single.just(listOf(patientWithErrors)))
    whenever(repository.updatePatientsSyncStatus(oldStatus = PENDING, newStatus = IN_FLIGHT)).thenReturn(Completable.complete())
    whenever(repository.updatePatientsSyncStatus(oldStatus = IN_FLIGHT, newStatus = DONE)).thenReturn(Completable.complete())
    whenever(repository.updatePatientsSyncStatus(listOf(patientUuid), INVALID)).thenReturn(Completable.complete())

    val validationErrors = ValidationErrors(patientUuid, listOf("some-schema-error-message"))
    whenever(api.push(any())).thenReturn(Single.just(DataPushResponse(listOf(validationErrors))))

    patientSync.push().blockingAwait()

    verify(repository).updatePatientsSyncStatus(PENDING, IN_FLIGHT)
    verify(repository).updatePatientsSyncStatus(listOf(patientUuid), INVALID)
  }

  @After
  fun tearDown() {
    reset(syncConfigProvider)
  }
}
