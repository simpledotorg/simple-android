package org.simple.clinic.appupdate

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.intellij.lang.annotations.Language
import org.simple.clinic.appconfig.Country
import org.simple.clinic.feature.Features
import org.simple.clinic.main.TypedMap
import org.simple.clinic.main.TypedMap.Type.UpdatePriorities
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.IsLightAppUpdateNotificationShown
import org.simple.clinic.main.TypedPreference.Type.IsMediumAppUpdateNotificationShown
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
      appVersionFetcher: AppVersionFetcher,
      @TypedMap(UpdatePriorities) updatePriorities: Map<String, Int>
  ): CheckAppUpdateAvailability {
    return CheckAppUpdateAvailability(
        config = appUpdateConfig,
        updateManager = updateManager,
        features = features,
        appVersionFetcher = appVersionFetcher,
        updatePriorities = updatePriorities
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

  @TypedPreference(IsLightAppUpdateNotificationShown)
  @Provides
  fun provideLightAppUpdateNudgeShownBoolean(
      rxSharedPreferences: RxSharedPreferences
  ): Preference<Boolean> {
    return rxSharedPreferences.getBoolean("is_light_app_update_notification_shown", false)
  }

  @TypedPreference(IsMediumAppUpdateNotificationShown)
  @Provides
  fun provideMediumAppUpdateNudgeShownBoolean(
      rxSharedPreferences: RxSharedPreferences
  ): Preference<Boolean> {
    return rxSharedPreferences.getBoolean("is_medium_app_update_notification_shown", false)
  }

  @TypedMap(UpdatePriorities)
  @Provides
  fun provideAppUpdatePriority(
      configReader: ConfigReader,
      moshi: Moshi
  ): Map<String, Int> {
    val type = Types.newParameterizedType(Map::class.java, String::class.java, Integer::class.java)
    val updatePrioritiesAdapter = moshi.adapter<Map<String, Int>>(type)

    @Language("JSON")
    val defaultJson = "{}"

    val json = configReader.string("update_priorities", defaultJson)

    return updatePrioritiesAdapter.fromJson(json)!!
  }
}
