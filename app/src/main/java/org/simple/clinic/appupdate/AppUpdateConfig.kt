package org.simple.clinic.appupdate

import io.reactivex.Observable
import org.simple.clinic.remoteconfig.ConfigReader

data class AppUpdateConfig(
    val differenceBetweenVersionsToNudge: Long
) {

  companion object {
    fun read(reader: ConfigReader): Observable<AppUpdateConfig> {
      return Observable.fromCallable {
        AppUpdateConfig(
            differenceBetweenVersionsToNudge = reader.long(name = "appupdate_version_diff_to_nudge", default = 1000))
      }
    }
  }
}
