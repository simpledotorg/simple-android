package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.home.overdue.OverdueAppointment
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
  fun config(): Single<AppointmentConfig> {
    return Single.just(AppointmentConfig(
        minimumOverduePeriodForHighRisk = Period.ofDays(30),
        overduePeriodForLowestRiskLevel = Period.ofDays(365),
        isAppointmentV3Enabled = false))
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
  fun syncApi(retrofit: Retrofit): AppointmentSyncApiV2 {
    return retrofit.create(AppointmentSyncApiV2::class.java)
  }

  @Provides
  @Named("last_appointment_pull_token")
  fun lastPullTokenV2(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_appointment_pull_token_v2", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}
