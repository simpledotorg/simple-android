package org.simple.clinic.bp.sync

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
import org.mockito.Mockito
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.sync.ValidationErrors
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import java.util.Collections
import java.util.UUID

class BloodPressureSyncTest {

  private val api: BloodPressureSyncApiV1 = mock()
  private val repository: BloodPressureRepository = mock()
  private val lastSyncTimestamp: Preference<Optional<Instant>> = mock()

  // This is a lambda just so that the config can be changed later.
  private val syncConfigProvider: () -> SyncConfig = mock()

  private lateinit var bpSync: BloodPressureSync

  @Before
  fun setup() {
    bpSync = BloodPressureSync(api, repository, Single.fromCallable { syncConfigProvider() }, lastSyncTimestamp)
  }

  @Test
  fun `when pending-sync measurements are empty then the server should not get called`() {
    whenever(repository.measurementsWithSyncStatus(SyncStatus.PENDING)).thenReturn(Single.just(Collections.emptyList()))

    bpSync.push().blockingAwait()

    verify(api, never()).push(any())
  }

  @Test
  fun `when a push succeeds then any measurements that were created or updated during the push started should not get marked as synced`() {
    // TODO: I'm out of ideas to write this test because it involves concurrency.
  }

  @Test
  fun `errors during push should not affect pull`() {
    whenever(repository.measurementsWithSyncStatus(any())).thenReturn(Single.error(NullPointerException()))

    val config = SyncConfig(mock(), batchSize = 10)
    whenever(syncConfigProvider()).thenReturn(config)
    whenever(lastSyncTimestamp.asObservable()).thenReturn(Observable.just(None))
    whenever(api.pull(config.batchSize)).thenReturn(Single.just(BloodPressurePullResponse(mock(), mock())))
    whenever(repository.mergeWithLocalData(any())).thenReturn(Completable.complete())

    bpSync.sync().test()
        .await()
        .assertError(NullPointerException::class.java)

    verify(api, never()).push(any())
    verify(api).pull(config.batchSize)
  }

  @Test
  fun `errors during pull should not affect push`() {
    whenever(repository.measurementsWithSyncStatus(SyncStatus.PENDING)).thenReturn(Single.just(listOf(mock())))
    whenever(repository.updateMeasurementsSyncStatus(oldStatus = SyncStatus.PENDING, newStatus = SyncStatus.IN_FLIGHT)).thenReturn(Completable.complete())
    whenever(api.push(any())).thenReturn(Single.just(DataPushResponse(listOf())))
    whenever(repository.updateMeasurementsSyncStatus(oldStatus = SyncStatus.IN_FLIGHT, newStatus = SyncStatus.DONE)).thenReturn(Completable.complete())

    whenever(syncConfigProvider()).thenThrow(AssertionError())

    bpSync.sync()
        .test()
        .await()
        .assertError(AssertionError::class.java)

    verify(api, never()).pull(any(), any())
    verify(api).push(any())
  }

  @Test
  fun `if there are validation errors during push, then the flagged patient should be marked as invalid`() {
    val measurementUuid = UUID.randomUUID()
    val measurementWithErrors = PatientMocker.bp(measurementUuid)

    whenever(repository.measurementsWithSyncStatus(SyncStatus.PENDING)).thenReturn(Single.just(listOf(measurementWithErrors)))
    whenever(repository.updateMeasurementsSyncStatus(oldStatus = SyncStatus.PENDING, newStatus = SyncStatus.IN_FLIGHT)).thenReturn(Completable.complete())
    whenever(repository.updateMeasurementsSyncStatus(oldStatus = SyncStatus.IN_FLIGHT, newStatus = SyncStatus.DONE)).thenReturn(Completable.complete())
    whenever(repository.updateMeasurementsSyncStatus(listOf(measurementUuid), SyncStatus.INVALID)).thenReturn(Completable.complete())

    val validationErrors = ValidationErrors(measurementUuid, listOf("some-schema-error-message"))
    whenever(api.push(any())).thenReturn(Single.just(DataPushResponse(listOf(validationErrors))))

    bpSync.push().blockingAwait()

    verify(repository).updateMeasurementsSyncStatus(SyncStatus.PENDING, SyncStatus.IN_FLIGHT)
    verify(repository).updateMeasurementsSyncStatus(listOf(measurementUuid), SyncStatus.INVALID)
  }

  @After
  fun tearDown() {
    Mockito.reset(syncConfigProvider)
  }
}
