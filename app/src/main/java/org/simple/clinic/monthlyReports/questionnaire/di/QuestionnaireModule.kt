package org.simple.clinic.monthlyReports.questionnaire.di

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnairePullToken
import org.simple.clinic.monthlyReports.questionnaire.Questionnaire
import org.simple.clinic.monthlyReports.questionnaire.sync.QuestionnaireSyncApi
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
open class QuestionnaireModule {

  @Provides
  fun dao(appDatabase: AppDatabase): Questionnaire.RoomDao {
    return appDatabase.questionnaireDao()
  }

  @Provides
  fun syncApi(@Named("for_deployment") retrofit: Retrofit): QuestionnaireSyncApi {
    return retrofit.create(QuestionnaireSyncApi::class.java)
  }

  @Provides
  @TypedPreference(LastQuestionnairePullToken)
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_questionnaire_pull_token_v1", StringPreferenceConverter())
  }
}
