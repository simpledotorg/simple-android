package org.simple.clinic.appupdate

import android.app.Application
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.remoteconfig.ConfigReader

@Module
open class AppUpdateModule {

  @Provides
  fun appUpdateConfig(reader: ConfigReader) = AppUpdateConfig
      .read(reader)
      // For experiments
      .map { it.copy(inAppUpdateEnabled = false) }


  @Provides
  fun checkAppUpdate(application: Application, appUpdateConfig: Observable<AppUpdateConfig>) =
      CheckAppUpdateAvailability(
          appContext = application,
          config = appUpdateConfig
      )

}
