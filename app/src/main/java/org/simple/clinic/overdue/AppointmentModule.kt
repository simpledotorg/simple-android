package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.AppDatabase
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.scheduleappointment.ScheduleAppointmentIn
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.simple.clinic.util.StringPreferenceConverter
import org.threeten.bp.Period
import retrofit2.Retrofit
import javax.inject.Named

@Module
class AppointmentModule {

  @Provides
  fun config(): Observable<AppointmentConfig> {
    return Observable.just(AppointmentConfig(
        minimumOverduePeriodForHighRisk = Period.ofDays(30),
        overduePeriodForLowestRiskLevel = Period.ofDays(365),
        appointmentDuePeriodForDefaulters = Period.ofDays(30),
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

  @Provides
  fun dao(appDatabase: AppDatabase): Appointment.RoomDao {
    return appDatabase.appointmentDao()
  }

  @Provides
  fun overdueAppointmentDao(appDatabase: AppDatabase): OverdueAppointment.RoomDao {
    return appDatabase.overdueAppointmentDao()
  }

  @Provides
  fun syncApiV3(retrofit: Retrofit): AppointmentSyncApi {
    return retrofit.create(AppointmentSyncApi::class.java)
  }

  @Provides
  @Named("last_appointment_pull_token")
  fun lastPullTokenV3(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_appointment_pull_token_v3", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}
