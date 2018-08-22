package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.threeten.bp.Instant
import retrofit2.Retrofit
import javax.inject.Named

@Module
class FollowUpScheduleModule {

  @Provides
  fun dao(appDatabase: AppDatabase): FollowUpSchedule.RoomDao {
    return appDatabase.followUpScheduleDao()
  }

  @Provides
  fun syncApi(retrofit: Retrofit): FollowUpScheduleSyncApiV1 {
    return retrofit.create(FollowUpScheduleSyncApiV1::class.java)
  }

  @Provides
  @Named("last_followupschedule_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_bp_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
