package org.simple.clinic.sync

import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.filterAndUnwrapJust
import javax.inject.Inject

class SyncDataOnApproval @Inject constructor(
    private val userSession: UserSession,
    private val dataSync: DataSync
) : IDataSyncOnApproval {

  override fun sync() {
    val shouldSync = userSession
        .loggedInUser()
        .filterAndUnwrapJust()
        .map { it.status }
        .buffer(2, 1)
        .filter { (previousStatus, currentStatus) -> currentStatus == UserStatus.ApprovedForSyncing && previousStatus != UserStatus.ApprovedForSyncing }
        .map { Any() }

    shouldSync
        .observeOn(io())
        .flatMapCompletable { dataSync.sync(null) }
        .subscribe()
  }
}
