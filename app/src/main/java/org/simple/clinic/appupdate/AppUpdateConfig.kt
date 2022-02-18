package org.simple.clinic.appupdate

import io.reactivex.Observable
import org.simple.clinic.remoteconfig.ConfigReader

data class AppUpdateConfig(
    val differenceBetweenVersionsToNudge: Long,
    val differenceBetweenVersionsForLightNudge: Long,
    val differenceBetweenVersionsForMediumNudge: Long,
    val differenceBetweenVersionsForCriticalNudge: Long
) {

  companion object {
    fun read(reader: ConfigReader): Observable<AppUpdateConfig> {
      return Observable.fromCallable {
        AppUpdateConfig(
            differenceBetweenVersionsToNudge = reader.long(name = "appupdate_version_diff_to_nudge", default = 1000),
            differenceBetweenVersionsForLightNudge = reader.long(name = "appupdate_min_version_difference_for_light_nudge", default = 30),
            differenceBetweenVersionsForMediumNudge = reader.long(name = "appupdate_min_version_difference_for_medium_nudge", default = 61),
            differenceBetweenVersionsForCriticalNudge = reader.long(name = "appupdate_min_version_difference_for_critical_nudge", default = 181)
        )
      }
    }
  }
}
