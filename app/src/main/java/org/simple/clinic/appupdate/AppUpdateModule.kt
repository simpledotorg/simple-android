package org.simple.clinic.appupdate

import android.app.Application
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.feature.Features
import org.simple.clinic.remoteconfig.ConfigReader

@Module
open class AppUpdateModule {

  @Provides
  fun appUpdateConfig(reader: ConfigReader) = AppUpdateConfig.read(reader)

  @Provides
  fun appUpdateManager(application: Application): AppUpdateManager = AppUpdateManagerFactory.create(application)

  @Provides
  fun checkAppUpdate(
      application: Application,
      appUpdateConfig: Observable<AppUpdateConfig>,
      updateManager: PlayUpdateManager,
      features: Features
  ): CheckAppUpdateAvailability {
    return CheckAppUpdateAvailability(
        appContext = application,
        config = appUpdateConfig,
        updateManager = updateManager,
        features = features
    )
  }

}
