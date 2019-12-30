package org.simple.clinic.bloodsugar.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.bloodsugar.BloodSugarMeasurement

@Module
class BloodSugarModule {

  @Provides
  fun dao(appDatabase: AppDatabase): BloodSugarMeasurement.RoomDao {
    return appDatabase.bloodSugarDao()
  }
}
