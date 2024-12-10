package org.simple.clinic.cvdrisk

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase

@Module
class CVDRiskModule {
  @Provides
  fun dao(appDatabase: AppDatabase): CVDRisk.RoomDao {
    return appDatabase.cvdRiskDao()
  }
}
