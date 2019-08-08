package org.simple.clinic.overdue

import io.reactivex.Observable
import org.simple.clinic.scheduleappointment.ScheduleAppointmentConfig
import org.simple.clinic.scheduleappointment.ScheduleAppointmentIn
import org.threeten.bp.Period

data class AppointmentConfig(
    val minimumOverduePeriodForHighRisk: Period,
    val overduePeriodForLowestRiskLevel: Period,
    val appointmentDuePeriodForDefaulters: Period,
    val scheduleAppointmentConfigProvider: Observable<ScheduleAppointmentConfig>
) {
  val periodsToScheduleAppointmentsIn: List<ScheduleAppointmentIn>
    get() = scheduleAppointmentConfigProvider.blockingFirst().periodsToScheduleAppointmentsIn

  val scheduleAppointmentInByDefault: ScheduleAppointmentIn
    get() = scheduleAppointmentConfigProvider.blockingFirst().scheduleAppointmentInByDefault
}
