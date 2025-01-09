package org.simple.clinic.cvdrisk

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.cvdrisk.sync.CVDRiskSyncApi
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastCVDRiskPullToken
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
class CVDRiskModule {
  @Provides
  fun dao(appDatabase: AppDatabase): CVDRisk.RoomDao {
    return appDatabase.cvdRiskDao()
  }

  @Provides
  fun syncApi(@Named("for_deployment") retrofit: Retrofit): CVDRiskSyncApi {
    return retrofit.create(CVDRiskSyncApi::class.java)
  }

  @Provides
  @TypedPreference(LastCVDRiskPullToken)
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_cvd_risk_pull_token", StringPreferenceConverter())
  }

  @Provides
  @OptIn(ExperimentalStdlibApi::class)
  fun cvdRiskCalculationSheet(moshi: Moshi, configReader: ConfigReader): CVDRiskCalculationSheet? {
    val adapter = moshi.adapter<CVDRiskCalculationSheet>()
    val json = configReader.string("cvd_risk_calculation_sheet_v0", "{}")

    return try {
      adapter.fromJson(json)
    } catch (e: Throwable) {
      CrashReporter.report(e)
      null
    }
  }
}
