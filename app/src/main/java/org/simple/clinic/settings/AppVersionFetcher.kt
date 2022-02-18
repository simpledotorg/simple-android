package org.simple.clinic.settings

import android.content.pm.PackageInfo
import javax.inject.Inject

class AppVersionFetcher @Inject constructor(
    private val packageInfo: PackageInfo
) {

  fun appVersion(applicationId: String): String {
    val packageInfo = packageManager.getPackageInfo(applicationId, 0)
    return packageInfo.versionName
  }
}
