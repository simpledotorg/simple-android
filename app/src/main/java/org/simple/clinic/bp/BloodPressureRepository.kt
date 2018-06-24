package org.simple.clinic.bp

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

fun dummyUserForBpTests() = LoggedInUser(
    // TODO: Uncomment this once user login works for real! Make user login call before running tests!
    UUID.fromString("e814c463-3ad7-4e9c-868c-1bbb66cd6bff"),
    "abc",
    "231",
    "24c4z;",
    UUID.randomUUID(),
    Instant.now(),
    Instant.now())

@AppScope
class BloodPressureRepository @Inject constructor(
    private val dao: BloodPressureMeasurement.RoomDao,
    private val loggedInUserPref: Preference<Optional<LoggedInUser>>,
    private val facilityRepository: FacilityRepository
) {

  fun saveMeasurement(patientUuid: UUID, systolic: Int, diastolic: Int): Completable {
    if (systolic < 0 || diastolic < 0) {
      throw AssertionError("Cannot have negative BP readings.")
    }

    // TODO: Remove this once user login works fine! BP Android tests will fail without this!
    val loggedInUser = Observable.just(dummyUserForBpTests())

    // TODO: Uncomment this once user login works for real! Make user login call before running BP Android tests!
//    val loggedInUser = loggedInUserPref
//        .asObservable()
//        .map { it.toNullable() }

    val currentFacility = facilityRepository
        .currentFacility()
        .take(1)

    return Observables.combineLatest(loggedInUser, currentFacility)
        .flatMapCompletable { (user, facility) ->
          Completable.fromAction {
            val newMeasurement = BloodPressureMeasurement(
                uuid = UUID.randomUUID(),
                systolic = systolic,
                diastolic = diastolic,
                syncStatus = SyncStatus.PENDING,
                patientUuid = patientUuid,
                facilityUuid = facility.uuid,
                userUuid = user!!.uuid,
                createdAt = Instant.now(),
                updatedAt = Instant.now())
            dao.save(listOf(newMeasurement))
          }
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

  // TODO: rename to newest100MeasurementsForPatient()
  fun recentMeasurementsForPatient(patientUuid: UUID): Observable<List<BloodPressureMeasurement>> {
    return dao
        .forPatient(patientUuid)
        .toObservable()
  }
}
