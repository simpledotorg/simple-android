package org.simple.clinic.appupdate

import android.app.Application
import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader

@Module
open class AppUpdateModule {

  @Provides
  fun appUpdateConfig(reader: ConfigReader) = AppUpdateConfig.read(reader)


  @Provides
  fun checkAppUpdate(application: Application, appUpdateConfig: AppUpdateConfig) =
      CheckAppUpdateAvailability(
          appContext = application,
          config = appUpdateConfig
      )

}
