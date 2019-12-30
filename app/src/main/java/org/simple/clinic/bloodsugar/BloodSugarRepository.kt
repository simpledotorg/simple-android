package org.simple.clinic.bloodsugar

import io.reactivex.Completable
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
      recordedAt: Instant
  ): Completable {
    return Completable.fromAction {
      dao.save(listOf(
          BloodSugarMeasurement(
              uuid = UUID.randomUUID(),
              reading = reading,
              recordedAt = recordedAt,
              patientUuid = patientUuid,
              userUuid = loggedInUser.uuid,
              facilityUuid = facility.uuid,
              timestamps = Timestamps.create(utcClock),
              syncStatus = SyncStatus.PENDING
          )
      ))
    }
  }
}
