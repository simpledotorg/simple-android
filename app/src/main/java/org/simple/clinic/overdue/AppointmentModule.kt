package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.overdue.TimeToAppointment.Months
import org.simple.clinic.overdue.TimeToAppointment.Weeks
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.time.Period
import javax.inject.Named

@Module
class AppointmentModule {

  @Provides
  fun config(): AppointmentConfig {
    return AppointmentConfig(
        appointmentDuePeriodForDefaulters = Period.ofDays(30),
        scheduleAppointmentsIn = scheduleAppointmentDays() + scheduleAppointmentMonths(),
        defaultTimeToAppointment = Days(28),
        periodForIncludingOverdueAppointments = Period.ofMonths(12),
        remindAppointmentsIn = remindAppointmentDays() + remindAppointmentWeeks()
    )
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
  fun syncApiV3(@Named("for_country") retrofit: Retrofit): AppointmentSyncApi {
    return retrofit.create(AppointmentSyncApi::class.java)
  }

  @Provides
  @Named("last_appointment_pull_token")
  fun lastPullTokenV3(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_appointment_pull_token_v3", StringPreferenceConverter())
  }

  private fun scheduleAppointmentDays(): List<TimeToAppointment> {
    return listOf(
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
        Days(11),
        Days(12),
        Days(13),
        Days(14),
        Days(15),
        Days(16),
        Days(17),
        Days(18),
        Days(19),
        Days(20),
        Days(21),
        Days(22),
        Days(23),
        Days(24),
        Days(25),
        Days(26),
        Days(27),
        Days(28),
        Days(29)
    )
  }

  private fun scheduleAppointmentMonths(): List<TimeToAppointment> {
    return listOf(
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
    )
  }

  private fun remindAppointmentDays(): List<TimeToAppointment> {
    return listOf(
        Days(1),
        Days(2),
        Days(3),
        Days(4),
        Days(5),
        Days(6),
        Days(7)
    )
  }

  private fun remindAppointmentWeeks(): List<TimeToAppointment> {
    return listOf(
        Weeks(2),
        Weeks(3),
        Weeks(4),
        Weeks(5),
        Weeks(6),
        Weeks(7),
        Weeks(8),
        Weeks(9),
        Weeks(10),
        Weeks(11),
        Weeks(12)
    )
  }
}
