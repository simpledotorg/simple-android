package org.simple.clinic.overdue

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.UserSession
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    val dao: Appointment.RoomDao,
    val userSession: UserSession,
    val facilityRepository: FacilityRepository
) : SynceableRepository<Appointment, AppointmentPayload> {

  fun schedule(patientUuid: UUID, appointmentDate: LocalDate): Completable {
    val newAppointmentStream = facilityRepository
        .currentFacility(userSession)
        .take(1)
        .map { facility ->
          Appointment(
              uuid = UUID.randomUUID(),
              patientUuid = patientUuid,
              facilityUuid = facility.uuid,
              date = appointmentDate,
              status = Appointment.Status.SCHEDULED,
              statusReason = Appointment.StatusReason.NOT_CALLED_YET,
              syncStatus = SyncStatus.PENDING,
              createdAt = Instant.now(),
              updatedAt = Instant.now())
        }
        .flatMapCompletable { save(listOf(it)) }

    return cancelScheduledAppointments(patientUuid).andThen(newAppointmentStream)
  }

  private fun cancelScheduledAppointments(patientId: UUID): Completable {
    return Completable.fromAction {
      dao.cancelScheduledAppointmentsForPatient(
          patientId = patientId,
          cancelledStatus = Appointment.Status.CANCELLED,
          scheduledStatus = Appointment.Status.SCHEDULED,
          newSyncStatus = SyncStatus.PENDING)
    }
  }

  fun save(appointments: List<Appointment>): Completable {
    return Completable.fromAction {
      dao.save(appointments)
    }
  }

  fun overdueAppointments(): Observable<List<Appointment>> {
    return dao.appointments(
        scheduledStatus = Appointment.Status.SCHEDULED,
        dateNow = LocalDate.now()
    ).toObservable()
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
    val newOrUpdatedAppointments = payloads
        .filter { payload: AppointmentPayload ->
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { toDatabaseModel(it, SyncStatus.DONE) }
        .toList()

    return Completable.fromAction { dao.save(newOrUpdatedAppointments) }
  }

  private fun toDatabaseModel(payload: AppointmentPayload, syncStatus: SyncStatus): Appointment {
    return payload.run {
      Appointment(
          uuid = uuid,
          facilityUuid = facilityUuid,
          patientUuid = patientUuid,
          date = date,
          status = status,
          statusReason = statusReason,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt)
    }
  }
}
