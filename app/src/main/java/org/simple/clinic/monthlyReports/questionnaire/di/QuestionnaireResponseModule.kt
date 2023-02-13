package org.simple.clinic.monthlyReports.questionnaire.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.questionnaireResponse.QuestionnaireResponse

@Module
open class QuestionnaireResponseModule {
  @Provides
  fun dao(appDatabase: AppDatabase): QuestionnaireResponse.RoomDao {
    return appDatabase.questionnaireResponseDao()
  }
}
