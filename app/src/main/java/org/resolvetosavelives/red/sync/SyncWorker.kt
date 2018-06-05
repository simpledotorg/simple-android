package org.resolvetosavelives.red.sync

import androidx.work.Worker
import io.reactivex.Completable
import io.reactivex.Single
import org.resolvetosavelives.red.RedApp
import org.resolvetosavelives.red.bp.sync.BloodPressureSync
import org.resolvetosavelives.red.sync.patient.PatientSync
import javax.inject.Inject

class SyncWorker : Worker() {

  companion object {
    const val TAG = "patient-sync"
  }

  @Inject
  lateinit var patientSync: PatientSync

  @Inject
  lateinit var bloodPressureSync: BloodPressureSync

  override fun doWork(): WorkerResult {
    RedApp.appComponent.inject(this)

    return Completable.mergeArrayDelayError(patientSync.sync(), bloodPressureSync.sync())
        .onErrorComplete()
        .andThen(Single.just(WorkerResult.SUCCESS))
        .blockingGet()
  }
}
