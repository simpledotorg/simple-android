package org.simple.clinic.sync

import io.reactivex.disposables.Disposable
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class SyncDataOnApproval @Inject constructor(
    private val userSession: UserSession,
    private val dataSync: DataSync,
    private val schedulersProvider: SchedulersProvider
) : IDataSyncOnApproval {

  /**
   * We want to do this only at the specific moment where the user's status changes from
   * anything to `ApprovedForSyncing`.
   *
   * By buffering 2 emissions and skipping 1 value instead of 2, we get a sliding window of size 2
   * of the changes in the user status and we can explicitly check for the case where a user
   * becomes `ApprovedForSyncing` from some other state.
   **/
  override fun sync(): Disposable {
    val syncSignal = userSession
        .loggedInUser()
        .filterAndUnwrapJust()
        .map { it.status }
        .buffer(2, 1)
        .filter { (previousStatus, currentStatus) -> currentStatus == ApprovedForSyncing && previousStatus != ApprovedForSyncing }
        .map { Unit }

    return syncSignal.subscribe { dataSync.fireAndForgetSync() }
  }
}
