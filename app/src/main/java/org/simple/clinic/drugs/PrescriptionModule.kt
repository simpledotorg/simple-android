package org.simple.clinic.drugs

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.drugs.sync.PrescriptionSyncApi
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
class PrescriptionModule {

  @Provides
  fun dao(appDatabase: AppDatabase): PrescribedDrug.RoomDao {
    return appDatabase.prescriptionDao()
  }

  @Provides
  fun syncApi(@Named("for_deployment") retrofit: Retrofit): PrescriptionSyncApi {
    return retrofit.create(PrescriptionSyncApi::class.java)
  }

  @Provides
  @Named("last_prescription_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_prescription_pull_token_v2", StringPreferenceConverter())
  }

  @Provides
  @OptIn(ExperimentalStdlibApi::class)
  fun diagnosisWarningPrescriptions(moshi: Moshi, configReader: ConfigReader): DiagnosisWarningPrescriptions {
    val adapter = moshi.adapter<DiagnosisWarningPrescriptions>()
    val json = configReader.string("diagnosis_warning_prescriptions_v0", "{}")

    return try {
      adapter.fromJson(json)!!
    } catch (e: Throwable) {
      CrashReporter.report(e)
      DiagnosisWarningPrescriptions.empty()
    }
  }
}
