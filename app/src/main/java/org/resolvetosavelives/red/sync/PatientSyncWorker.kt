package org.resolvetosavelives.red.sync

import androidx.work.Worker
import io.reactivex.Single
import org.resolvetosavelives.red.RedApp
import javax.inject.Inject

class PatientSyncWorker : Worker() {

  companion object {
    const val TAG = "patient-sync"
  }

  @Inject
  lateinit var patientSync: PatientSync

  override fun doWork(): WorkerResult {
    RedApp.appComponent.inject(this)

    return patientSync.sync()
        .onErrorComplete()
        .andThen(Single.just(WorkerResult.SUCCESS))
        .blockingGet()
  }
}
