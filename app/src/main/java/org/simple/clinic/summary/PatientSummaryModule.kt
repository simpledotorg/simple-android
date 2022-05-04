package org.simple.clinic.summary

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewConfig
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfigModule
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultFacilityInfoApi
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordModule
import retrofit2.Retrofit
import java.util.UUID
import javax.inject.Named

@Module(includes = [BloodSugarSummaryConfigModule::class, TeleconsultRecordModule::class])
class PatientSummaryModule {

  @Provides
  fun teleconsultationFacilitySyncApi(@Named("for_deployment") retrofit: Retrofit): TeleconsultFacilityInfoApi {
    return retrofit.create(TeleconsultFacilityInfoApi::class.java)
  }

  @Provides
  fun providesBloodPressureSummaryConfig(configReader: ConfigReader): BloodPressureSummaryViewConfig {
    return BloodPressureSummaryViewConfig.read(configReader)
  }

  @Provides
  fun missingPhoneReminderDao(appDatabase: AppDatabase) = appDatabase.missingPhoneReminderDao()

  @Provides
  fun teleconsultationFacilityDao(appDatabase: AppDatabase) = appDatabase.teleconsultFacilityInfoDao()

  @Provides
  fun medicalOfficersDao(appDatabase: AppDatabase) = appDatabase.teleconsultMedicalOfficersDao()

  @Provides
  fun teleconsultationFacilityWithMedicalOfficersDao(appDatabase: AppDatabase) = appDatabase.teleconsultFacilityWithMedicalOfficersDao()

  // TODO (SM): Remove once CDSS pilot is finished
  @Provides
  fun cdssPilotFacilitiesIds(
      moshi: Moshi,
      configReader: ConfigReader
  ): List<UUID> {
    val type = Types.newParameterizedType(List::class.java, UUID::class.java)
    val adapter = moshi.adapter<List<UUID>>(type)
    val json = configReader.string("cdss_pilot_facilities_ids", "[]")

    return try {
      adapter.fromJson(json)!!
    } catch (e: Exception) {
      CrashReporter.report(e)
      emptyList()
    }
  }
}
