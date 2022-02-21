package org.simple.clinic.appupdate

import android.app.Application
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.appconfig.Country
import org.simple.clinic.feature.Features
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.settings.AppVersionFetcher
import org.simple.clinic.util.toOptional
import java.util.Optional

@Module
open class AppUpdateModule {

  @Provides
  fun appUpdateConfig(reader: ConfigReader) = AppUpdateConfig.read(reader)

  @Provides
  fun appUpdateManager(application: Application): AppUpdateManager = AppUpdateManagerFactory.create(application)

  @Provides
  fun checkAppUpdate(
      appUpdateConfig: Observable<AppUpdateConfig>,
      updateManager: PlayUpdateManager,
      features: Features,
      appVersionFetcher: AppVersionFetcher
  ): CheckAppUpdateAvailability {
    return CheckAppUpdateAvailability(
        config = appUpdateConfig,
        updateManager = updateManager,
        features = features,
        appVersionFetcher = appVersionFetcher
    )
  }

  @Provides
  fun providesAppUpdateHelpContactBasedOnCountry(
      remoteConfigReader: ConfigReader,
      moshi: Moshi,
      country: Country
  ): Optional<AppUpdateHelpContact> {
    val type = Types.newParameterizedType(Map::class.java, String::class.java, AppUpdateHelpContact::class.java)
    val configAdapter = moshi.adapter<Map<String, AppUpdateHelpContact>>(type)

    val appUpdateHelpContactJson = remoteConfigReader.string("app_update_help_contact", "")
    val appUpdateHelpContacts: Map<String, AppUpdateHelpContact> = configAdapter.fromJson(appUpdateHelpContactJson)!!

    return appUpdateHelpContacts[country.isoCountryCode].toOptional()
  }
}
