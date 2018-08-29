package org.simple.clinic.overdue

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

class AppointmentRepository @Inject constructor(val dao: Appointment.RoomDao) : SynceableRepository<Appointment, AppointmentPayload> {

  fun save(vararg schedules: Appointment): Completable {
    return Completable.fromAction {
      dao.save(schedules.toList())
    }
  }

  override fun pendingSyncRecords(): Single<List<Appointment>> {
    return dao.withSyncStatus(SyncStatus.PENDING).firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return Completable.fromAction { dao.updateSyncStatus(from, to) }
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    if (ids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction { dao.updateSyncStatus(ids, to) }
  }

  override fun mergeWithLocalData(payloads: List<AppointmentPayload>): Completable {
    val newOrUpdatedSchedules = payloads
        .filter { payload: AppointmentPayload ->
          val localCopy = dao.getOne(payload.id)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { toDatabaseModel(it, SyncStatus.DONE) }
        .toList()

    return Completable.fromAction { dao.save(newOrUpdatedSchedules) }
  }

  private fun toDatabaseModel(payload: AppointmentPayload, syncStatus: SyncStatus): Appointment {
    return payload.run {
      Appointment(
          id = id,
          facilityId = facilityId,
          patientId = patientId,
          date = date,
          status = status,
          statusReason = statusReason,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt)
    }
  }
}
