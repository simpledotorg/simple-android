package org.simple.clinic.teleconsultlog.teleconsultrecord

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase

@Module
class TeleconsultRecordModule {
  @Provides
  fun teleconsultRecordDao(appDatabase: AppDatabase) = appDatabase.teleconsultRecordDao()

  @Provides
  fun teleconsultRecordPrescribedDrugsDao(appDatabase: AppDatabase) = appDatabase.teleconsultRecordPrescribedDrugDao()

  @Provides
  fun teleconsultRecordWithPrescribedDrugDao(appDatabase: AppDatabase) = appDatabase.teleconsultRecordWithPrescribedDrugDao()
}
