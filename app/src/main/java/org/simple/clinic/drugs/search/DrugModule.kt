package org.simple.clinic.drugs.search

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase

@Module
object DrugModule {

  @Provides
  fun drugDao(database: AppDatabase): Drug.RoomDao {
    return database.drugDao()
  }
}
