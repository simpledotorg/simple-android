package org.simple.clinic.bp

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.user.UserSession
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class BloodPressureRepository @Inject constructor(
    private val dao: BloodPressureMeasurement.RoomDao,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) {

  fun saveMeasurement(patientUuid: UUID, systolic: Int, diastolic: Int): Single<BloodPressureMeasurement> {
    if (systolic < 0 || diastolic < 0) {
      throw AssertionError("Cannot have negative BP readings.")
    }

    val loggedInUser = userSession.requireLoggedInUser()
        .firstOrError()

    val currentFacility = facilityRepository
        .currentFacility(userSession)
        .firstOrError()

    return Singles.zip(loggedInUser, currentFacility)
        .map { (user, facility) ->
          BloodPressureMeasurement(
              uuid = UUID.randomUUID(),
              systolic = systolic,
              diastolic = diastolic,
              syncStatus = SyncStatus.PENDING,
              patientUuid = patientUuid,
              facilityUuid = facility.uuid,
              userUuid = user!!.uuid,
              createdAt = Instant.now(),
              updatedAt = Instant.now())
        }
        .flatMap {
          Completable
              .fromAction { dao.save(listOf(it)) }
              .toSingleDefault(it)
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
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { it.toDatabaseModel(SyncStatus.DONE) }
        .toList()
        .flatMapCompletable { Completable.fromAction { dao.save(it) } }
  }

  fun measurementCount(): Single<Int> {
    return dao.count().firstOrError()
  }

  fun newest100MeasurementsForPatient(patientUuid: UUID): Observable<List<BloodPressureMeasurement>> {
    return dao
        .newest100ForPatient(patientUuid)
        .toObservable()
  }

  fun findOne(uuid: UUID): Single<BloodPressureMeasurement> = Single.fromCallable { dao.getOne(uuid) }
}
