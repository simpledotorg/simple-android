package org.simple.clinic.illustration

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase

@Module
class HomescreenIllustrationModule {

  @Provides
  fun illustrationDao(appDatabase: AppDatabase) = appDatabase.illustrationDao()
}
