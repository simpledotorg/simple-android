package org.simple.clinic.overdue.callresult.di

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastCallResultPullToken
import org.simple.clinic.overdue.callresult.CallResultSyncApi
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
class CallResultModule {

  @Provides
  fun provideRoomDao(appDatabase: AppDatabase) = appDatabase.callResultDao()

  @Provides
  fun provideApi(@Named("for_deployment") retrofit: Retrofit): CallResultSyncApi {
    return retrofit.create(CallResultSyncApi::class.java)
  }

  @Provides
  @TypedPreference(LastCallResultPullToken)
  fun lastPullToken(rxSharedPreferences: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPreferences.getOptional("last_call_result_pull_token", StringPreferenceConverter())
  }
}
