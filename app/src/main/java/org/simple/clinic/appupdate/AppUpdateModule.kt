package org.simple.clinic.appupdate

import android.app.Application
import dagger.Module
import dagger.Provides

@Module
open class AppUpdateModule {

  @Provides
  fun appUpdateConfig() = AppUpdateConfig(
      inAppUpdateEnabled = false,
      differenceBetweenVersionsToNudge = 1000)


  @Provides
  fun checkAppUpdate(application: Application, appUpdateConfig: AppUpdateConfig) =
      CheckAppUpdateAvailability(
          appContext = application,
          config = appUpdateConfig
      )

}
