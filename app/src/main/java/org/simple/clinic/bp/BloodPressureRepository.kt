package org.simple.clinic.bp

import androidx.paging.DataSource
import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class BloodPressureRepository @Inject constructor(
    private val dao: BloodPressureMeasurement.RoomDao,
    private val utcClock: UtcClock
) : SynceableRepository<BloodPressureMeasurement, BloodPressureMeasurementPayload> {

  fun saveMeasurement(
      patientUuid: UUID,
      reading: BloodPressureReading,
      loggedInUser: User,
      currentFacility: Facility,
      recordedAt: Instant,
      uuid: UUID
  ): BloodPressureMeasurement {
    if (reading.systolic < 0 || reading.diastolic < 0) {
      throw AssertionError("Cannot have negative BP readings.")
    }

    val now = Instant.now(utcClock)

    val bloodPressureMeasurement = BloodPressureMeasurement(
        uuid = uuid,
        reading = reading,
        syncStatus = SyncStatus.PENDING,
        userUuid = loggedInUser.uuid,
        facilityUuid = currentFacility.uuid,
        patientUuid = patientUuid,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        recordedAt = recordedAt
    )

    dao.save(listOf(bloodPressureMeasurement))

    return bloodPressureMeasurement
  }

  override fun save(records: List<BloodPressureMeasurement>): Completable {
    return Completable.fromAction { dao.save(records) }
  }

  fun updateMeasurement(measurement: BloodPressureMeasurement) {
    val updatedMeasurement = measurement.copy(
        updatedAt = Instant.now(utcClock),
        syncStatus = SyncStatus.PENDING
    )

    dao.save(listOf(updatedMeasurement))
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): List<BloodPressureMeasurement> {
    return dao.withSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    dao.updateSyncStatus(oldStatus = from, newStatus = to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    dao.updateSyncStatusForIds(uuids = ids, newStatus = to)
  }

  override fun mergeWithLocalData(payloads: List<BloodPressureMeasurementPayload>) {
    val dirtyRecords = dao.recordIdsWithStatus(SyncStatus.PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { it.toDatabaseModel(SyncStatus.DONE) }

    dao.save(payloadsToSave)
  }

  override fun recordCount(): Observable<Int> {
    return dao.count().toObservable()
  }

  fun newestMeasurementsForPatient(patientUuid: UUID, limit: Int): Observable<List<BloodPressureMeasurement>> {
    return dao
        .newestMeasurementsForPatient(patientUuid, limit)
        .toObservable()
  }

  fun newestMeasurementsForPatientImmediate(patientUuid: UUID, limit: Int): List<BloodPressureMeasurement> {
    return dao.newestMeasurementsForPatientImmediate(patientUuid, limit)
  }

  fun measurement(uuid: UUID): Observable<BloodPressureMeasurement> = dao.bloodPressure(uuid).toObservable()

  fun measurementImmediate(uuid: UUID): BloodPressureMeasurement = dao.bloodPressureImmediate(uuid)

  fun markBloodPressureAsDeleted(bloodPressureMeasurement: BloodPressureMeasurement): Completable {
    return Completable.fromAction {
      val now = Instant.now(utcClock)
      val deletedBloodPressureMeasurement = bloodPressureMeasurement.copy(
          updatedAt = now,
          deletedAt = now,
          syncStatus = SyncStatus.PENDING)

      dao.save(listOf(deletedBloodPressureMeasurement))
    }
  }

  fun bloodPressureCountImmediate(patientUuid: UUID): Int = dao.recordedBloodPressureCountForPatientImmediate(patientUuid)

  fun bloodPressureCount(patientUuid: UUID): Observable<Int> = dao.recordedBloodPressureCountForPatient(patientUuid)

  fun allBloodPressures(patientUuid: UUID): Observable<List<BloodPressureMeasurement>> {
    return dao.allBloodPressures(patientUuid)
  }

  fun allBloodPressuresDataSource(patientUuid: UUID): DataSource.Factory<Int, BloodPressureMeasurement> {
    return dao.allBloodPressuresDataSource(patientUuid)
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return dao
        .count(SyncStatus.PENDING)
        .toObservable()
  }
}
