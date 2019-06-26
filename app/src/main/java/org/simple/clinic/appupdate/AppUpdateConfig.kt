package org.simple.clinic.appupdate

import org.simple.clinic.remoteconfig.ConfigReader

data class AppUpdateConfig(
    val inAppUpdateEnabled: Boolean,
    val differenceBetweenVersionsToNudge: Long
) {

  companion object {
    fun read(reader: ConfigReader): AppUpdateConfig {
      return AppUpdateConfig(
          inAppUpdateEnabled = reader.boolean(name = "appupdate_enabled", default = false),
          differenceBetweenVersionsToNudge = reader.long(name = "appupdate_version_diff_to_nudge", default = 1000))
    }
  }
}
