package org.simple.clinic.sync

import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.user.UserSession
import timber.log.Timber
import javax.inject.Inject

class SyncDataOnApproval @Inject constructor(
    private val userSession: UserSession,
    private val dataSync: DataSync
) : IDataSyncOnApproval {

  override fun sync() {
    val shouldSync = userSession
        .canSyncData()
        .distinctUntilChanged()
        .filter { canSyncData -> canSyncData }

    shouldSync
        .observeOn(io())
        .flatMapCompletable {
          Timber.tag("SyncWork").i("Sync from SyncDataOnApproval")
          dataSync.sync(null)
        }
        .subscribe()
  }
}
