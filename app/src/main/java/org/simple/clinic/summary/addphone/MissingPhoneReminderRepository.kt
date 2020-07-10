package org.simple.clinic.summary.addphone

import io.reactivex.Completable
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.util.UtcClock
import java.time.Instant
import javax.inject.Inject

class MissingPhoneReminderRepository @Inject constructor(
    private val dao: MissingPhoneReminder.RoomDao,
    val utcClock: UtcClock
) {

  fun hasShownReminderForPatient(patientUuid: PatientUuid): Boolean {
    return dao.forPatient(patientUuid) != null
  }

  fun markReminderAsShownFor(patientUuid: PatientUuid): Completable {
    return Completable.fromAction {
      dao.save(MissingPhoneReminder(patientUuid, remindedAt = Instant.now(utcClock)))
    }
  }
}
