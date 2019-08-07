package org.simple.clinic.scheduleappointment

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.threeten.bp.temporal.ChronoUnit

@Module
class ScheduleAppointmentModule {

  @Provides
  fun providesScheduleAppointmentConfig(): Observable<ScheduleAppointmentConfig> {
    return Observable.just(ScheduleAppointmentConfig(
        possibleAppointments = listOf(
            ScheduleAppointment(1, ChronoUnit.DAYS),
            ScheduleAppointment(2, ChronoUnit.DAYS),
            ScheduleAppointment(3, ChronoUnit.DAYS),
            ScheduleAppointment(4, ChronoUnit.DAYS),
            ScheduleAppointment(5, ChronoUnit.DAYS),
            ScheduleAppointment(6, ChronoUnit.DAYS),
            ScheduleAppointment(7, ChronoUnit.DAYS),
            ScheduleAppointment(8, ChronoUnit.DAYS),
            ScheduleAppointment(9, ChronoUnit.DAYS),
            ScheduleAppointment(10, ChronoUnit.DAYS),
            ScheduleAppointment(2, ChronoUnit.WEEKS),
            ScheduleAppointment(3, ChronoUnit.WEEKS),
            ScheduleAppointment(1, ChronoUnit.MONTHS),
            ScheduleAppointment(2, ChronoUnit.MONTHS),
            ScheduleAppointment(3, ChronoUnit.MONTHS),
            ScheduleAppointment(4, ChronoUnit.MONTHS),
            ScheduleAppointment(5, ChronoUnit.MONTHS),
            ScheduleAppointment(6, ChronoUnit.MONTHS),
            ScheduleAppointment(7, ChronoUnit.MONTHS),
            ScheduleAppointment(8, ChronoUnit.MONTHS),
            ScheduleAppointment(9, ChronoUnit.MONTHS),
            ScheduleAppointment(10, ChronoUnit.MONTHS),
            ScheduleAppointment(11, ChronoUnit.MONTHS),
            ScheduleAppointment(12, ChronoUnit.MONTHS)
        ),
        defaultAppointment = ScheduleAppointment(1, ChronoUnit.MONTHS)
    ))
  }
}
