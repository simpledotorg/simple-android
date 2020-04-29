package org.simple.clinic.settings

import android.content.pm.PackageManager
import javax.inject.Inject

class AppVersionFetcher @Inject constructor(
    private val packageManager: PackageManager
) {

  fun appVersion(applicationId: String): String {
    val packageInfo = packageManager.getPackageInfo(applicationId, 0)
    return packageInfo.versionName
  }
}
