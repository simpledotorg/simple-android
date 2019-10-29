package org.simple.clinic.bp

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.di.AppScope
import org.simple.clinic.encounter.EncounterRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.User
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.generateEncounterUuid
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class BloodPressureRepository @Inject constructor(
    private val dao: BloodPressureMeasurement.RoomDao,
    private val encounterRepository: EncounterRepository,
    private val utcClock: UtcClock,
    private val userClock: UserClock
) {

  fun saveMeasurement(
      patientUuid: UUID,
      systolic: Int,
      diastolic: Int,
      loggedInUser: User,
      currentFacility: Facility,
      recordedAt: Instant = Instant.now(utcClock)
  ): Single<BloodPressureMeasurement> {
    if (systolic < 0 || diastolic < 0) {
      throw AssertionError("Cannot have negative BP readings.")
    }

    val now = Instant.now(utcClock)
    val encounteredDate = recordedAt.toLocalDateAtZone(userClock.zone)
    val encounterUuid = generateEncounterUuid(currentFacility.uuid, patientUuid, encounteredDate)

    val bloodPressureMeasurement = BloodPressureMeasurement(
        uuid = UUID.randomUUID(),
        systolic = systolic,
        diastolic = diastolic,
        syncStatus = SyncStatus.PENDING,
        userUuid = loggedInUser.uuid,
        facilityUuid = currentFacility.uuid,
        patientUuid = patientUuid,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        recordedAt = recordedAt,
        encounterUuid = encounterUuid)

    return encounterRepository.saveBloodPressureMeasurement(bloodPressureMeasurement)
        .toSingleDefault(bloodPressureMeasurement)
  }

  fun updateMeasurement(measurement: BloodPressureMeasurement): Completable {
    return encounterRepository.updateBloodPressure(measurement)
  }

  //TODO: Remove this method once its usage is replaced in ReportPendingRecordsToAnalytics
  fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<BloodPressureMeasurement>> {
    return dao
        .withSyncStatus(syncStatus)
        .firstOrError()
  }

  fun newestMeasurementsForPatient(patientUuid: UUID, limit: Int): Observable<List<BloodPressureMeasurement>> {
    return dao
        .newestMeasurementsForPatient(patientUuid, limit)
        .toObservable()
  }

  fun measurement(uuid: UUID): Observable<BloodPressureMeasurement> = dao.bloodPressure(uuid).toObservable()

  fun markBloodPressureAsDeleted(bloodPressureMeasurement: BloodPressureMeasurement): Completable {
    return Completable.fromAction {
      val now = Instant.now(utcClock)
      val deletedBloodPressureMeasurement = bloodPressureMeasurement.copy(
          updatedAt = now,
          deletedAt = now,
          syncStatus = SyncStatus.PENDING)

      dao.save(listOf(deletedBloodPressureMeasurement))
    }.andThen(encounterRepository.deleteEncounter(bloodPressureMeasurement.encounterUuid))
  }

  fun bloodPressureCount(patientUuid: UUID): Observable<Int> {
    return dao
        .recordedBloodPressureCountForPatient(patientUuid)
        .toObservable()
  }

  fun haveBpsForPatientChangedSince(patientUuid: UUID, instant: Instant): Observable<Boolean> {
    return dao
        .haveBpsForPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = instant,
            pendingStatus = SyncStatus.PENDING
        )
        .toObservable()
  }
}
