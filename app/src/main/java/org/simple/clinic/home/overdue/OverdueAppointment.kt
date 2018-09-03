package org.simple.clinic.home.overdue

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.threeten.bp.LocalDate

data class OverdueAppointment(

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    @Embedded(prefix = "appt_")
    val appointment: Appointment,

    @Embedded(prefix = "bp_")
    val bloodPressure: BloodPressureMeasurement
) {

  @Dao
  interface RoomDao {

    @Query("""
          SELECT P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.age_computedDateOfBirth,

          BP.uuid bp_uuid, BP.systolic bp_systolic, BP.diastolic bp_diastolic, BP.syncStatus bp_syncStatus, BP.userUuid bp_userUuid,
          BP.facilityUuid bp_facilityUuid, BP.patientUuid bp_patientUuid, BP.createdAt bp_createdAt, BP.updatedAt bp_updatedAt,

          A.uuid appt_uuid, A.patientUuid appt_patientUuid, A.facilityUuid appt_facilityUuid, A.date appt_date, A.status appt_status,
          A.statusReason appt_statusReason, A.syncStatus appt_syncStatus, A.createdAt appt_createdAt, A.updatedAt appt_updatedAt

          FROM Patient P

          INNER JOIN Appointment A ON A.patientUuid = P.uuid
          INNER JOIN BloodPressureMeasurement BP ON BP.patientUuid = P.uuid

          WHERE A.status = :scheduledStatus AND A.date < :dateNow
          GROUP BY P.uuid HAVING max(BP.updatedAt)
          ORDER BY A.date ASC
          """)
    fun appointments(scheduledStatus: Appointment.Status, dateNow: LocalDate): Flowable<List<OverdueAppointment>>
  }
}
