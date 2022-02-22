package org.simple.clinic.settings

import android.content.pm.PackageInfo
import android.os.Build
import javax.inject.Inject

class AppVersionFetcher @Inject constructor(
    private val packageInfo: PackageInfo
) {

  fun appVersion(): String {
    return packageInfo.versionName
  }

  fun appVersionCode(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      packageInfo.longVersionCode.toInt()
    } else {
      packageInfo.versionCode
    }
  }
}
