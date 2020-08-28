package org.simple.clinic.bloodsugar

import androidx.paging.DataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.bloodsugar.sync.BloodSugarMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class BloodSugarRepository @Inject constructor(
    val dao: BloodSugarMeasurement.RoomDao,
    val utcClock: UtcClock
) : SynceableRepository<BloodSugarMeasurement, BloodSugarMeasurementPayload> {

  fun saveMeasurement(
      reading: BloodSugarReading,
      patientUuid: UUID,
      loggedInUser: User,
      facility: Facility,
      recordedAt: Instant,
      uuid: UUID
  ): Single<BloodSugarMeasurement> {
    return Single
        .just(
            BloodSugarMeasurement(
                uuid = uuid,
                reading = reading,
                recordedAt = recordedAt,
                patientUuid = patientUuid,
                userUuid = loggedInUser.uuid,
                facilityUuid = facility.uuid,
                timestamps = Timestamps.create(utcClock),
                syncStatus = PENDING
            )
        )
        .flatMap {
          Completable
              .fromAction { dao.save(listOf(it)) }
              .toSingleDefault(it)
        }
  }

  fun latestMeasurements(patientUuid: UUID, limit: Int): Observable<List<BloodSugarMeasurement>> {
    return dao.latestMeasurements(patientUuid, limit)
  }

  fun latestMeasurementsImmediate(patientUuid: UUID, limit: Int): List<BloodSugarMeasurement> {
    return dao.latestMeasurementsImmediate(patientUuid, limit)
  }

  fun allBloodSugars(patientUuid: UUID): Observable<List<BloodSugarMeasurement>> {
    return dao.allBloodSugars(patientUuid)
  }

  fun allBloodSugarsDataSource(patientUuid: UUID): DataSource.Factory<Int, BloodSugarMeasurement> {
    return dao.allBloodSugarsDataSource(patientUuid)
  }

  fun bloodSugarsCount(patientUuid: UUID): Observable<Int> = dao.recordedBloodSugarsCountForPatient(patientUuid)

  fun bloodSugarCountImmediate(patientUuid: UUID): Int = dao.recordedBloodSugarsCountForPatientImmediate(patientUuid)

  override fun save(records: List<BloodSugarMeasurement>): Completable =
      Completable.fromAction { dao.save(records) }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): List<BloodSugarMeasurement> {
    return dao.withSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    dao.updateSyncStatus(oldStatus = from, newStatus = to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    dao.updateSyncStatus(uuids = ids, newStatus = to)
  }

  override fun mergeWithLocalData(payloads: List<BloodSugarMeasurementPayload>) {
    val dirtyRecords = dao.recordIdsWithSyncStatus(PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { it.toDatabaseModel(SyncStatus.DONE) }

    dao.save(payloadsToSave)
  }

  override fun recordCount(): Observable<Int> =
      dao.count().toObservable()

  override fun pendingSyncRecordCount(): Observable<Int> =
      dao.count(PENDING).toObservable()

  fun measurement(bloodSugarMeasurementUuid: UUID): BloodSugarMeasurement? =
      dao.getOne(bloodSugarMeasurementUuid)

  fun updateMeasurement(measurement: BloodSugarMeasurement) {
    val updatedMeasurement = measurement.copy(
        timestamps = measurement.timestamps.copy(
            updatedAt = Instant.now(utcClock)
        ),
        syncStatus = PENDING
    )

    dao.save(listOf(updatedMeasurement))
  }

  fun markBloodSugarAsDeleted(bloodSugarMeasurement: BloodSugarMeasurement) {
    val deletedBloodSugarMeasurement = bloodSugarMeasurement.copy(
        timestamps = bloodSugarMeasurement.timestamps.delete(utcClock),
        syncStatus = PENDING
    )
    dao.save(listOf(deletedBloodSugarMeasurement))
  }
}
