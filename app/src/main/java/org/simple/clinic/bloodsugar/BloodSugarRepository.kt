package org.simple.clinic.bloodsugar

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class BloodSugarRepository @Inject constructor(
    val dao: BloodSugarMeasurement.RoomDao,
    val utcClock: UtcClock
) {

  fun saveMeasurement(
      reading: BloodSugarReading,
      patientUuid: UUID,
      loggedInUser: User,
      facility: Facility,
      recordedAt: Instant,
      uuid: UUID = UUID.randomUUID()
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
                syncStatus = SyncStatus.PENDING
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

  fun allBloodSugars(patientUuid: UUID): Observable<List<BloodSugarMeasurement>> {
    return dao.allBloodSugars(patientUuid)
  }
}
