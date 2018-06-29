package org.simple.clinic.sync

import androidx.work.Worker
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.ClinicApp
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.user.UserSession
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class SyncWorker : Worker() {

  companion object {
    const val TAG = "patient-sync"
  }

  @Inject
  lateinit var patientSync: PatientSync

  @Inject
  lateinit var bloodPressureSync: BloodPressureSync

  @Inject
  lateinit var prescriptionSync: PrescriptionSync

  @Inject
  lateinit var userSession: UserSession

  override fun doWork(): WorkerResult {
    ClinicApp.appComponent.inject(this)

    if (userSession.isUserLoggedIn().not()) {
      Timber.i("User isn't logged in yet. Skipping sync.")
      return WorkerResult.SUCCESS
    }

    return patientSync.sync()
        .andThen(Completable.mergeArrayDelayError(bloodPressureSync.sync(), prescriptionSync.sync()))
        .doOnError {
          if (it !is IOException) {
            Timber.e(it)
          }
        }
        .onErrorComplete()
        .andThen(Single.just(WorkerResult.SUCCESS))
        .blockingGet()
  }
}
