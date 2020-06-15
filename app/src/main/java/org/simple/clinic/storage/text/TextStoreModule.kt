package org.simple.clinic.storage.text

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase

@Module
class TextStoreModule {

  @Provides
  fun provideDao(appDatabase: AppDatabase): TextRecord.RoomDao = appDatabase.textRecordDao()

  @Provides
  fun provideTextStore(
      dao: TextRecord.RoomDao
  ): TextStore {
    return LocalDbTextStore(dao)
  }
}
