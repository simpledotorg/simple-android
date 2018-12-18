package org.simple.clinic.protocol

import android.annotation.SuppressLint
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import javax.inject.Inject

@SuppressLint("CheckResult")
class SyncProtocolsOnLogin @Inject constructor(
    private val userSession: UserSession,
    private val protocolSync: ProtocolSync,
    private val protocolRepository: ProtocolRepository
) {

  fun listen() {
    userSession.loggedInUser()
        .withLatestFrom(protocolRepository.recordCount())
        .subscribeOn(io())
        .filter { (user, drugCount) -> user.isNotEmpty() && drugCount == 0 }
        .flatMapCompletable { protocolSync.sync() }
        .onErrorComplete()
        .subscribe()
  }
}
