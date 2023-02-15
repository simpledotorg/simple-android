package org.simple.clinic.questionnaireresponse.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Module
open class QuestionnaireResponseModule {
  @Provides
  fun dao(appDatabase: AppDatabase): QuestionnaireResponse.RoomDao {
    return appDatabase.questionnaireResponseDao()
  }
}
