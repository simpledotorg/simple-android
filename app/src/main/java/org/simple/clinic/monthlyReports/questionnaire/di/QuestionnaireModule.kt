package org.simple.clinic.monthlyReports.questionnaire.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.monthlyReports.questionnaire.Questionnaire

@Module
open class QuestionnaireModule {

  @Provides
  fun dao(appDatabase: AppDatabase): Questionnaire.RoomDao {
    return appDatabase.questionnaireDao()
  }
}
