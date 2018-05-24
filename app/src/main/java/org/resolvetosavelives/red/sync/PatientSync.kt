package org.resolvetosavelives.red.sync

import io.reactivex.Completable
import timber.log.Timber
import javax.inject.Inject

class PatientSync @Inject constructor() {

  fun sync(): Completable {
    return Completable.fromAction({
      Timber.i("[TODO] Here lies the greatest algorithm ever written for syncing patients with the server.")
    })
  }
}
