package org.simple.clinic.user

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.ResolvedError.Unauthorized
import javax.inject.Inject

class UnauthorizeUser @Inject constructor(
    private val userSession: UserSession,
    private val dataSync: DataSync
) {

  fun listen(scheduler: Scheduler) {
    dataSync
        .streamSyncErrors()
        .observeOn(scheduler)
        .ofType<Unauthorized>()
        .flatMapCompletable { userSession.unauthorize() }
        .subscribe()
  }
}
