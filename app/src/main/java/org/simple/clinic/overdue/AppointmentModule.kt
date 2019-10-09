package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.AppDatabase
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.scheduleappointment.TimeToAppointment.Days
import org.simple.clinic.scheduleappointment.TimeToAppointment.Months
import org.simple.clinic.scheduleappointment.TimeToAppointment.Weeks
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.OptionalRxPreferencesConverter
import org.simple.clinic.util.preference.StringPreferenceConverter
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
        scheduleAppointmentsIn = listOf(
            Days(1),
            Days(2),
            Days(3),
            Days(4),
            Days(5),
            Days(6),
            Days(7),
            Days(8),
            Days(9),
            Days(10),
            Weeks(2),
            Weeks(3),
            Months(1),
            Months(2),
            Months(3),
            Months(4),
            Months(5),
            Months(6),
            Months(7),
            Months(8),
            Months(9),
            Months(10),
            Months(11),
            Months(12)
        ),
        defaultTimeToAppointment = Months(1)
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
