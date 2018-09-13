package org.simple.clinic.sync

import androidx.work.Worker
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.ClinicApp
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.overdue.AppointmentSync
import org.simple.clinic.overdue.communication.CommunicationSync
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
  lateinit var userSession: UserSession

  @Inject
  lateinit var patientSync: PatientSync

  @Inject
  lateinit var bloodPressureSync: BloodPressureSync

  @Inject
  lateinit var prescriptionSync: PrescriptionSync

  @Inject
  lateinit var appointmentSync: AppointmentSync

  @Inject
  lateinit var communicationSync: CommunicationSync

  @Inject
  lateinit var medicalHistorySync: MedicalHistorySync

  @Inject
  lateinit var facilitySync: FacilitySync

  override fun doWork(): WorkerResult {
    ClinicApp.appComponent.inject(this)

    return userSession.loggedInUser()
        .firstOrError()
        .map { (user) -> user?.isApprovedForSyncing() ?: false }
        .flatMap { isApproved ->
          when {
            isApproved -> sync()
            else -> Single.just(WorkerResult.SUCCESS)
          }
        }
        .blockingGet()
  }

  private fun sync(): Single<WorkerResult> {
    return patientSync.sync()
        .andThen(Completable.mergeArrayDelayError(
            bloodPressureSync.sync(),
            prescriptionSync.sync(),
            appointmentSync.sync(),
            communicationSync.sync(),
            medicalHistorySync.sync(),
            facilitySync.sync()
        ))
        .doOnError {
          if (it !is IOException) {
            Timber.e(it)
          }
        }
        .onErrorComplete()
        .andThen(Single.just(WorkerResult.SUCCESS))
  }
}
