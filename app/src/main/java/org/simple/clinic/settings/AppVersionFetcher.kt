package org.simple.clinic.settings

import android.content.pm.PackageInfo
import javax.inject.Inject

class AppVersionFetcher @Inject constructor(
    private val packageInfo: PackageInfo
) {

  fun appVersion(): String {
    return packageInfo.versionName
  }
}
