package org.simple.clinic.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.ClinicApp
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.overdue.AppointmentSync
import org.simple.clinic.overdue.communication.CommunicationSync
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.protocolv2.sync.ProtocolSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.exhaustive
import timber.log.Timber
import javax.inject.Inject

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

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

  @Inject
  lateinit var protocolSync: ProtocolSync

  @Inject
  lateinit var crashReporter: CrashReporter

  override fun doWork(): Result {
    ClinicApp.appComponent.inject(this)

    return userSession.loggedInUser()
        .firstOrError()
        .map { (user) -> user?.isApprovedForSyncing() ?: false }
        .flatMap { isApproved ->
          when {
            isApproved -> sync()
            else -> Single.just(Result.SUCCESS)
          }
        }
        .blockingGet()
  }

  private fun sync(): Single<Result> {
    return patientSync.sync()
        .andThen(Completable.mergeArrayDelayError(
            bloodPressureSync.sync(),
            prescriptionSync.sync(),
            appointmentSync.sync(),
            communicationSync.sync(),
            medicalHistorySync.sync(),
            facilitySync.sync(),
            protocolSync.sync()
        ))
        .doOnError(logError())
        .onErrorComplete()
        .andThen(Single.just(Result.SUCCESS))
  }

  private fun logError() = { e: Throwable ->
    val resolvedError = ErrorResolver.resolve(e)
    when (resolvedError) {
      is ResolvedError.Unexpected -> crashReporter.report(resolvedError.actualCause)
      is ResolvedError.NetworkRelated -> {
        // Connectivity issues are expected.
      }
    }.exhaustive()
    Timber.e(e)
  }
}
