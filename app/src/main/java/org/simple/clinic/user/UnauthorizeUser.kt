package org.simple.clinic.user

import io.reactivex.rxkotlin.ofType
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class UnauthorizeUser @Inject constructor(
    private val userSession: UserSession,
    private val dataSync: DataSync,
    private val schedulersProvider: SchedulersProvider
) {

  fun listen() {
    dataSync
        .streamSyncErrors()
        .observeOn(schedulersProvider.io())
        .ofType<Unauthenticated>()
        .flatMapCompletable { userSession.unauthorize() }
        .subscribe()
  }
}
