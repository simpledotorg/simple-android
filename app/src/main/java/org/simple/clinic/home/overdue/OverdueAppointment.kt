package org.simple.clinic.home.overdue

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumber
import org.threeten.bp.LocalDate
import java.util.UUID

data class OverdueAppointment(

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    @Embedded(prefix = "appt_")
    val appointment: Appointment,

    @Embedded(prefix = "bp_")
    val bloodPressure: BloodPressureMeasurement,

    @Embedded(prefix = "phone_")
    val phoneNumber: PatientPhoneNumber?
) {

  @Dao
  interface RoomDao {

    @Query("""
          SELECT P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.age_computedDateOfBirth,

          BP.uuid bp_uuid, BP.systolic bp_systolic, BP.diastolic bp_diastolic, BP.syncStatus bp_syncStatus, BP.userUuid bp_userUuid,
          BP.facilityUuid bp_facilityUuid, BP.patientUuid bp_patientUuid, BP.createdAt bp_createdAt, BP.updatedAt bp_updatedAt,

          A.uuid appt_uuid, A.patientUuid appt_patientUuid, A.facilityUuid appt_facilityUuid, A.scheduledDate appt_scheduledDate, A.status appt_status,
          A.cancelReason appt_cancelReason, A.remindOn appt_remindOn, A.agreedToVisit appt_agreedToVisit, A.syncStatus appt_syncStatus,
          A.createdAt appt_createdAt, A.updatedAt appt_updatedAt,

          PPN.uuid phone_uuid, PPN.patientUuid phone_patientUuid, PPN.number phone_number, PPN.phoneType phone_phoneType, PPN.active phone_active,
          PPN.createdAt phone_createdAt, PPN.updatedAt phone_updatedAt

          FROM Patient P

          INNER JOIN Appointment A ON A.patientUuid = P.uuid
          INNER JOIN BloodPressureMeasurement BP ON BP.patientUuid = P.uuid
          LEFT JOIN PatientPhoneNumber PPN ON PPN.patientUuid = P.uuid

          WHERE A.facilityUuid = :facilityUuid AND A.status = :scheduledStatus AND A.scheduledDate < :dateNow
          AND (A.remindOn < :dateNow OR A.remindOn IS NULL)

          GROUP BY P.uuid HAVING max(BP.updatedAt)
          ORDER BY A.scheduledDate, A.updatedAt ASC
          """)
    fun appointmentsForFacility(facilityUuid: UUID, scheduledStatus: Appointment.Status, dateNow: LocalDate): Flowable<List<OverdueAppointment>>
  }
}
