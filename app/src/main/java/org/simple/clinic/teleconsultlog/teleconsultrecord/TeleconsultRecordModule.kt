package org.simple.clinic.teleconsultlog.teleconsultrecord

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import retrofit2.Retrofit
import javax.inject.Named

@Module
class TeleconsultRecordModule {
  @Provides
  fun teleconsultRecordDao(appDatabase: AppDatabase) = appDatabase.teleconsultRecordDao()

  @Provides
  fun teleconsultRecordPrescribedDrugsDao(appDatabase: AppDatabase) = appDatabase.teleconsultRecordPrescribedDrugDao()

  @Provides
  fun teleconsultRecordWithPrescribedDrugDao(appDatabase: AppDatabase) = appDatabase.teleconsultRecordWithPrescribedDrugDao()

  @Provides
  fun teleconsultRecordSyncApi(@Named("for_country") retrofit: Retrofit) : TeleconsultRecordApi {
    return retrofit.create(TeleconsultRecordApi::class.java)
  }
}
