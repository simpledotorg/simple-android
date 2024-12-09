package org.simple.clinic.patientattribute

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase

@Module
class PatientAttributeModule {
  @Provides
  fun dao(appDatabase: AppDatabase): PatientAttribute.RoomDao {
    return appDatabase.patientAttributeDao()
  }
}
