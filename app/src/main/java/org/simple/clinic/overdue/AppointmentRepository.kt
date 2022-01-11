package org.simple.clinic.overdue

import androidx.paging.PagingSource
import io.reactivex.Observable
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.overdue.Appointment.AppointmentType
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.Appointment.Status.Visited
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.summary.nextappointment.NextAppointmentPatientProfile
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toOptional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    private val appointmentDao: Appointment.RoomDao,
    private val overdueDao: OverdueAppointment.RoomDao,
    private val utcClock: UtcClock,
    private val appointmentConfig: AppointmentConfig
) : SynceableRepository<Appointment, AppointmentPayload> {

  fun schedule(
      patientUuid: UUID,
      appointmentUuid: UUID,
      appointmentDate: LocalDate,
      appointmentType: AppointmentType,
      appointmentFacilityUuid: UUID,
      creationFacilityUuid: UUID
  ): Appointment {

    val appointment = Appointment(
        uuid = appointmentUuid,
        patientUuid = patientUuid,
        facilityUuid = appointmentFacilityUuid,
        scheduledDate = appointmentDate,
        status = Scheduled,
        cancelReason = null,
        remindOn = null,
        agreedToVisit = null,
        appointmentType = appointmentType,
        syncStatus = PENDING,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock),
        deletedAt = null,
        creationFacilityUuid = creationFacilityUuid
    )

    // TODO (vs) 20/05/20: Remove this side effect from this method
    markOlderAppointmentsAsVisited(patientUuid)
    appointmentDao.save(listOf(appointment))
    return appointment
  }

  private fun markOlderAppointmentsAsVisited(patientUuid: UUID) {
    appointmentDao.markOlderAppointmentsAsVisited(
        patientUuid = patientUuid,
        updatedStatus = Visited,
        scheduledStatus = Scheduled,
        newSyncStatus = PENDING,
        newUpdatedAt = Instant.now(utcClock)
    )
  }

  fun createReminder(appointmentUuid: UUID, reminderDate: LocalDate) {
    appointmentDao.saveRemindDate(
        appointmentUUID = appointmentUuid,
        reminderDate = reminderDate,
        newSyncStatus = PENDING,
        newUpdatedAt = Instant.now(utcClock)
    )
  }

  fun markAsAgreedToVisit(appointmentUuid: UUID, userClock: UserClock) {
    appointmentDao.markAsAgreedToVisit(
        appointmentUUID = appointmentUuid,
        reminderDate = LocalDate.now(userClock).plusMonths(1),
        newSyncStatus = PENDING,
        newUpdatedAt = Instant.now(utcClock)
    )
  }

  fun markAsAlreadyVisited(appointmentUuid: UUID) {
    appointmentDao.markAsVisited(
        appointmentUuid = appointmentUuid,
        newStatus = Visited,
        newSyncStatus = PENDING,
        newUpdatedAt = Instant.now(utcClock)
    )
  }

  fun cancelWithReason(appointmentUuid: UUID, reason: AppointmentCancelReason) {
    appointmentDao.cancelWithReason(
        appointmentUuid = appointmentUuid,
        cancelReason = reason,
        newStatus = Cancelled,
        newSyncStatus = PENDING,
        newUpdatedAt = Instant.now(utcClock)
    )
  }

  override fun save(records: List<Appointment>) {
    appointmentDao.save(records)
  }

  override fun recordCount(): Observable<Int> {
    return appointmentDao.count().toObservable()
  }

  fun overdueAppointmentsInFacility(
      since: LocalDate,
      facilityId: UUID
  ): PagingSource<Int, OverdueAppointment> {
    return overdueDao
        .overdueInFacilityPagingSource(
            facilityUuid = facilityId,
            scheduledBefore = since,
            scheduledAfter = since.minus(appointmentConfig.periodForIncludingOverdueAppointments)
        )
  }

  fun lastCreatedAppointmentForPatient(patientUuid: UUID): Optional<Appointment> {
    return appointmentDao.lastCreatedAppointmentForPatient(patientUuid).toOptional()
  }

  fun markAppointmentsCreatedBeforeTodayAsVisited(patientUuid: UUID) {
    val startOfToday = LocalDate
        .now(utcClock)
        .atStartOfDay()
        .toInstant(ZoneOffset.of(utcClock.zone.id))

    appointmentDao.markAsVisitedForPatient(
        patientUuid = patientUuid,
        updatedStatus = Visited,
        scheduledStatus = Scheduled,
        newSyncStatus = PENDING,
        newUpdatedAt = Instant.now(utcClock),
        createdBefore = startOfToday
    )

  }

  fun recordsWithSyncStatus(syncStatus: SyncStatus): List<Appointment> {
    return appointmentDao.recordsWithSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    appointmentDao.updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    appointmentDao.updateSyncStatusForIds(ids, to)
  }

  override fun mergeWithLocalData(payloads: List<AppointmentPayload>) {
    val dirtyRecords = appointmentDao.recordIdsWithSyncStatus(PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { toDatabaseModel(it, SyncStatus.DONE) }

    appointmentDao.save(payloadsToSave)
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
          deletedAt = deletedAt,
          creationFacilityUuid = creationFacilityUuid)
    }
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return appointmentDao
        .countWithStatus(PENDING)
        .toObservable()
  }

  override fun pendingSyncRecords(limit: Int, offset: Int): List<Appointment> {
    return appointmentDao
        .recordsWithSyncStatusBatched(
            syncStatus = PENDING,
            limit = limit,
            offset = offset
        )
  }

  fun latestOverdueAppointmentForPatient(
      patientUuid: UUID,
      date: LocalDate
  ): Optional<Appointment> {
    return appointmentDao.latestOverdueAppointmentForPatient(patientUuid, date).toOptional()
  }

  fun nextAppointmentPatientProfile(patientUuid: UUID): NextAppointmentPatientProfile? {
    return appointmentDao.nextAppointmentPatientProfile(patientUuid)
  }

  fun hasAppointmentForPatientChangedSince(patientUuid: UUID, timestamp: Instant): Boolean {
    return appointmentDao
        .hasAppointmentForPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = timestamp,
            pendingStatus = PENDING
        )
  }
}
