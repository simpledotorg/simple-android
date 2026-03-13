package org.simple.clinic.returnscore.di

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.returnscore.ReturnScore
import org.simple.clinic.returnscore.sync.ReturnScoreSyncApi
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
open class ReturnScoreModule {

  @Provides
  fun dao(appDatabase: AppDatabase): ReturnScore.RoomDao {
    return appDatabase.returnScoreDao()
  }

  @Provides
  fun syncApi(@Named("for_deployment") retrofit: Retrofit): ReturnScoreSyncApi {
    return retrofit.create(ReturnScoreSyncApi::class.java)
  }

  @Provides
  @TypedPreference(TypedPreference.Type.LastReturnScorePullToken)
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_return_score_pull_token_v1", StringPreferenceConverter())
  }
}

