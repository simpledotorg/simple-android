package org.simple.clinic.questionnaireresponse.di

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponseSyncApi
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
open class QuestionnaireResponseModule {
  @Provides
  fun dao(appDatabase: AppDatabase): QuestionnaireResponse.RoomDao {
    return appDatabase.questionnaireResponseDao()
  }

  @Provides
  fun syncApi(@Named("for_deployment") retrofit: Retrofit): QuestionnaireResponseSyncApi {
    return retrofit.create(QuestionnaireResponseSyncApi::class.java)
  }

  @Provides
  @TypedPreference(TypedPreference.Type.LastQuestionnaireResponsePullToken)
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_questionnaire_response_pull_token_v1", StringPreferenceConverter())
  }
}
