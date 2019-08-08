package org.simple.clinic.scheduleappointment

import dagger.Module
import dagger.Provides
import io.reactivex.Observable

@Module
class ScheduleAppointmentModule {

  @Provides
  fun providesScheduleAppointmentConfig(): Observable<ScheduleAppointmentConfig> {
    return Observable.just(ScheduleAppointmentConfig(
        periodsToScheduleAppointmentsIn = listOf(
            ScheduleAppointmentIn.days(1),
            ScheduleAppointmentIn.days(2),
            ScheduleAppointmentIn.days(3),
            ScheduleAppointmentIn.days(4),
            ScheduleAppointmentIn.days(5),
            ScheduleAppointmentIn.days(6),
            ScheduleAppointmentIn.days(7),
            ScheduleAppointmentIn.days(8),
            ScheduleAppointmentIn.days(9),
            ScheduleAppointmentIn.days(10),
            ScheduleAppointmentIn.weeks(2),
            ScheduleAppointmentIn.weeks(3),
            ScheduleAppointmentIn.months(1),
            ScheduleAppointmentIn.months(2),
            ScheduleAppointmentIn.months(3),
            ScheduleAppointmentIn.months(4),
            ScheduleAppointmentIn.months(5),
            ScheduleAppointmentIn.months(6),
            ScheduleAppointmentIn.months(7),
            ScheduleAppointmentIn.months(8),
            ScheduleAppointmentIn.months(9),
            ScheduleAppointmentIn.months(10),
            ScheduleAppointmentIn.months(11),
            ScheduleAppointmentIn.months(12)
        ),
        scheduleAppointmentInByDefault = ScheduleAppointmentIn.months(1)
    ))
  }
}
