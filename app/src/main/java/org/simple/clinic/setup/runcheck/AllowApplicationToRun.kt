package org.simple.clinic.setup.runcheck

import android.app.Application
import androidx.annotation.WorkerThread
import com.scottyab.rootbeer.RootBeer
import org.simple.clinic.BuildConfig
import javax.inject.Inject

class AllowApplicationToRun @Inject constructor(
    application: Application
) {

  private val rootBeer = RootBeer(application)

  @WorkerThread
  fun check(): AllowedToRun {
    val isDeviceRooted = rootBeer.isRooted
    val cannotRunOnRootedDevices = !BuildConfig.ALLOW_ROOTED_DEVICE

    return when {
      isDeviceRooted && cannotRunOnRootedDevices -> Disallowed(Disallowed.Reason.Rooted)
      else -> Allowed
    }
  }
}
