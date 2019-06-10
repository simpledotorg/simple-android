package org.simple.clinic.appupdate

import dagger.Module
import dagger.Provides

@Module
open class AppUpdateModule {

  @Provides
  fun appUpdateConfig() = AppUpdateConfig(
      inAppUpdateEnabled = false,
      differenceBetweenVersionsToNudge = 1000)
}
