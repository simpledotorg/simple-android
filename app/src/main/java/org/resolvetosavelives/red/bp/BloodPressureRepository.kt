package org.resolvetosavelives.red.bp

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.resolvetosavelives.red.bp.sync.BloodPressureMeasurementPayload
import org.resolvetosavelives.red.di.AppScope
import org.resolvetosavelives.red.patient.SyncStatus
import org.resolvetosavelives.red.patient.canBeOverriddenByServerCopy
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class BloodPressureRepository @Inject constructor(private val dao: BloodPressureMeasurement.RoomDao) {

  fun saveMeasurement(patientUuid: UUID, systolic: Int, diastolic: Int): Completable {
    if (systolic < 0 || diastolic < 0) {
      throw AssertionError("Cannot have negative BP readings.")
    }

    return Completable.fromAction {
      val newMeasurement = BloodPressureMeasurement(
          uuid = UUID.randomUUID(),
          systolic = systolic,
          diastolic = diastolic,
          createdAt = Instant.now(),
          updatedAt = Instant.now(),
          syncStatus = SyncStatus.PENDING,
          patientUuid = patientUuid)
      dao.save(newMeasurement)
    }
  }

  fun measurementsWithSyncStatus(status: SyncStatus): Single<List<BloodPressureMeasurement>> {
    return dao
        .withSyncStatus(status)
        .firstOrError()
  }

  fun updateMeasurementsSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus): Completable {
    return Completable.fromAction {
      dao.updateSyncStatus(oldStatus = oldStatus, newStatus = newStatus)
    }
  }

  fun updateMeasurementsSyncStatus(measurementUuids: List<UUID>, newStatus: SyncStatus): Completable {
    if (measurementUuids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction {
      dao.updateSyncStatus(uuids = measurementUuids, newStatus = newStatus)
    }
  }

  fun mergeWithLocalData(serverPayloads: List<BloodPressureMeasurementPayload>): Completable {
    return serverPayloads
        .toObservable()
        .filter { payload ->
          val localCopy = dao.get(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { it.toDatabaseModel(SyncStatus.DONE) }
        .toList()
        .flatMapCompletable { Completable.fromAction { dao.save(it) } }
  }

  fun measurementCount(): Single<Int> {
    return dao.measurementCount().firstOrError()
  }

  fun recentMeasurementsForPatient(patientUuid: UUID): Observable<List<BloodPressureMeasurement>> {
    return dao
        .measurementForPatient(patientUuid)
        .toObservable()
  }
}
