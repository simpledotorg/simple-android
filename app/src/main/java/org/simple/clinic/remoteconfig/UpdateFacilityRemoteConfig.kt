package org.simple.clinic.remoteconfig

import android.annotation.SuppressLint
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.WorkManager
import io.reactivex.Observable
import org.simple.clinic.facility.Facility
import org.simple.clinic.remoteconfig.UpdateRemoteConfigWorker.Companion.REMOTE_CONFIG_SYNC_WORKER
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class UpdateFacilityRemoteConfig @Inject constructor(
    private val currentFacilityStream: Observable<Facility>,
    private val workManager: WorkManager,
    private val schedulers: SchedulersProvider,
) {

  /**
   * When facility is changed, then update remote config, since
   * user properties are updated in Firebase which are used in
   * remote config conditionals
   *
   * https://firebase.google.com/docs/analytics/user-properties?platform=android
   */
  @SuppressLint("CheckResult")
  fun track() {
    currentFacilityStream
        .subscribeOn(schedulers.io())
        .subscribe {
          workManager.enqueueUniqueWork(REMOTE_CONFIG_SYNC_WORKER, REPLACE, UpdateRemoteConfigWorker.createWorkRequest())
        }
  }
}
