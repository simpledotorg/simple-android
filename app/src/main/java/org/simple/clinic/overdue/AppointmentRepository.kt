package org.simple.clinic.overdue

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.overdue.Appointment.AppointmentType
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status.CANCELLED
import org.simple.clinic.overdue.Appointment.Status.SCHEDULED
import org.simple.clinic.overdue.Appointment.Status.VISITED
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import java.util.UUID
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    private val appointmentDao: Appointment.RoomDao,
    private val overdueDao: OverdueAppointment.RoomDao,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val utcClock: UtcClock,
    private val appointmentConfigProvider: Single<AppointmentConfig>
) : SynceableRepository<Appointment, AppointmentPayload> {

  fun schedule(patientUuid: UUID, appointmentDate: LocalDate, appointmentType: AppointmentType): Single<Appointment> {
    val newAppointmentStream = facilityRepository
        .currentFacility(userSession)
        .firstOrError()
        .map { facility ->
          Appointment(
              uuid = UUID.randomUUID(),
              patientUuid = patientUuid,
              facilityUuid = facility.uuid,
              scheduledDate = appointmentDate,
              status = SCHEDULED,
              cancelReason = null,
              remindOn = null,
              agreedToVisit = null,
              appointmentType = appointmentType,
              syncStatus = SyncStatus.PENDING,
              createdAt = Instant.now(utcClock),
              updatedAt = Instant.now(utcClock),
              deletedAt = null)
        }
        .flatMap { appointment ->
          save(listOf(appointment)).andThen(Single.just(appointment))
        }

    return markOlderAppointmentsAsVisited(patientUuid).andThen(newAppointmentStream)
  }

  fun schedule(patientUuid: UUID, appointmentDate: LocalDate): Single<Appointment> {
    return schedule(patientUuid, appointmentDate, Manual)
  }

  private fun markOlderAppointmentsAsVisited(patientUuid: UUID): Completable {
    return Completable.fromAction {
      appointmentDao.markOlderAppointmentsAsVisited(
          patientUuid = patientUuid,
          updatedStatus = VISITED,
          scheduledStatus = SCHEDULED,
          newSyncStatus = SyncStatus.PENDING,
          newUpdatedAt = Instant.now(utcClock)
      )
    }
  }

  fun createReminder(appointmentUuid: UUID, reminderDate: LocalDate): Completable {
    return Completable.fromAction {
      appointmentDao.saveRemindDate(
          appointmentUUID = appointmentUuid,
          reminderDate = reminderDate,
          newSyncStatus = SyncStatus.PENDING,
          newUpdatedAt = Instant.now(utcClock)
      )
    }
  }

  fun markAsAgreedToVisit(appointmentUuid: UUID): Completable {
    return Completable.fromAction {
      appointmentDao.markAsAgreedToVisit(
          appointmentUUID = appointmentUuid,
          reminderDate = LocalDate.now(utcClock).plusDays(30),
          newSyncStatus = SyncStatus.PENDING,
          newUpdatedAt = Instant.now(utcClock))
    }
  }

  fun markAsAlreadyVisited(appointmentUuid: UUID): Completable {
    return Completable.fromAction {
      appointmentDao.markAsVisited(
          appointmentUuid = appointmentUuid,
          newStatus = VISITED,
          newSyncStatus = SyncStatus.PENDING,
          newUpdatedAt = Instant.now(utcClock))
    }
  }

  fun cancelWithReason(appointmentUuid: UUID, reason: AppointmentCancelReason): Completable {
    return Completable.fromAction {
      appointmentDao.cancelWithReason(
          appointmentUuid = appointmentUuid,
          cancelReason = reason,
          newStatus = CANCELLED,
          newSyncStatus = SyncStatus.PENDING,
          newUpdatedAt = Instant.now(utcClock))
    }
  }

  override fun save(records: List<Appointment>): Completable {
    return Completable.fromAction {
      appointmentDao.save(records)
    }
  }

  override fun recordCount(): Observable<Int> {
    return appointmentDao.count().toObservable()
  }

  fun overdueAppointments(): Observable<List<OverdueAppointment>> {
    val facilityUuidStream = facilityRepository.currentFacility(userSession)
        .map { it.uuid }

    val appointmentConfigStream = appointmentConfigProvider.toObservable()

    return Observables.combineLatest(facilityUuidStream, appointmentConfigStream)
        .flatMap { (facilityUuid, appointmentConfig) ->
          val today = LocalDate.now(utcClock)
          overdueDao
              .appointmentsForFacility(
                  facilityUuid = facilityUuid,
                  scheduledStatus = SCHEDULED,
                  scheduledBefore = today,
                  minimumOverdueDateForHighRisk = today.minus(appointmentConfig.minimumOverduePeriodForHighRisk),
                  overdueDateForLowestRiskLevel = today.minus(appointmentConfig.overduePeriodForLowestRiskLevel)
              ).toObservable()
        }
  }

  fun lastCreatedAppointmentForPatient(patientUuid: UUID): Observable<Optional<Appointment>> {
    return appointmentDao.lastCreatedAppointmentForPatient(patientUuid)
        .toObservable()
        .map { appointments ->
          when {
            appointments.isNotEmpty() -> Just(appointments.first())
            else -> None
          }
        }
  }

  fun markAppointmentsCreatedBeforeTodayAsVisited(patientUuid: UUID): Completable {
    val startOfToday = LocalDate
        .now(utcClock)
        .atStartOfDay()
        .toInstant(ZoneOffset.of(utcClock.zone.id))

    return Completable.fromAction {
      appointmentDao.markAsVisited(
          patientUuid = patientUuid,
          updatedStatus = VISITED,
          scheduledStatus = SCHEDULED,
          newSyncStatus = SyncStatus.PENDING,
          newUpdatedAt = Instant.now(utcClock),
          createdBefore = startOfToday
      )
    }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<Appointment>> {
    return appointmentDao.recordsWithSyncStatus(syncStatus).firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return Completable.fromAction { appointmentDao.updateSyncStatus(from, to) }
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    if (ids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction { appointmentDao.updateSyncStatus(ids, to) }
  }

  override fun mergeWithLocalData(payloads: List<AppointmentPayload>): Completable {
    val newOrUpdatedAppointments = payloads
        .asSequence()
        .filter { payload ->
          val localCopy = appointmentDao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { toDatabaseModel(it, SyncStatus.DONE) }
        .toList()

    return Completable.fromAction { appointmentDao.save(newOrUpdatedAppointments) }
  }

  private fun toDatabaseModel(payload: AppointmentPayload, syncStatus: SyncStatus): Appointment {
    return payload.run {
      Appointment(
          uuid = uuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = date,
          status = status,
          cancelReason = cancelReason,
          remindOn = remindOn,
          agreedToVisit = agreedToVisit,
          appointmentType = appointmentType,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt)
    }
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return appointmentDao
        .count(SyncStatus.PENDING)
        .toObservable()
  }
}
