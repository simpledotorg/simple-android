package org.simple.clinic.appupdate

import io.reactivex.Observable
import org.simple.clinic.remoteconfig.ConfigReader

data class AppUpdateConfig(
    val inAppUpdateEnabled: Boolean,
    val differenceBetweenVersionsToNudge: Long
) {

  companion object {
    fun read(reader: ConfigReader): Observable<AppUpdateConfig> {
      return Observable.fromCallable {
        AppUpdateConfig(
            inAppUpdateEnabled = reader.boolean(name = "appupdate_enabled", default = false),
            differenceBetweenVersionsToNudge = reader.long(name = "appupdate_version_diff_to_nudge", default = 1000))
      }
    }
  }
}
