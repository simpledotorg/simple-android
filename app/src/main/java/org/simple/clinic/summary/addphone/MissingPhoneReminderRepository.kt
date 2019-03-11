package org.simple.clinic.summary.addphone

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.patient.PatientUuid
import javax.inject.Inject

class MissingPhoneReminderRepository @Inject constructor(private val dao: MissingPhoneReminder.RoomDao) {

  fun hasShownReminderFor(patientUuid: PatientUuid): Single<Boolean> {
    return dao.get(patientUuid)
        .map { reminders -> reminders.isNotEmpty() }
        .firstOrError()
  }

  fun markReminderAsShownFor(patientUuid: PatientUuid): Completable {
    return Completable.fromAction {
      dao.save(MissingPhoneReminder(patientUuid, reminded = true))
    }
  }
}
