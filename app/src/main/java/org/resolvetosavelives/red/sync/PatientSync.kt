package org.resolvetosavelives.red.sync

import io.reactivex.Completable
import org.resolvetosavelives.red.newentry.search.PatientRepository
import timber.log.Timber
import javax.inject.Inject

class PatientSync @Inject constructor(private val api: PatientSyncApiV1, private val repository: PatientRepository) {

  fun sync(): Completable {
    return Completable.fromAction({
      Timber.i("[TODO] Here lies the greatest algorithm ever written for syncing patients with the server.")
    })
  }
}
