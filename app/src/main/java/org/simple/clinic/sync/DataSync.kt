package org.simple.clinic.sync

import io.reactivex.Completable
import org.simple.clinic.ClinicApp
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.overdue.AppointmentSync
import org.simple.clinic.overdue.communication.CommunicationSync
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.exhaustive
import timber.log.Timber
import javax.inject.Inject

class DataSync @Inject constructor(
    private val userSession: UserSession,
    private val patientSync: PatientSync,
    private val bloodPressureSync: BloodPressureSync,
    private val prescriptionSync: PrescriptionSync,
    private val appointmentSync: AppointmentSync,
    private val communicationSync: CommunicationSync,
    private val medicalHistorySync: MedicalHistorySync,
    private val facilitySync: FacilitySync,
    private val protocolSync: ProtocolSync,
    private val crashReporter: CrashReporter
) {

  fun syncIfUserIsApproved(): Completable {
    ClinicApp.appComponent.inject(this)

    return userSession.loggedInUser()
        .firstOrError()
        .map { (user) -> user?.isApprovedForSyncing() ?: false }
        .flatMapCompletable { isApproved ->
          when {
            isApproved -> sync()
            else -> Completable.complete()
          }
        }
  }

  private fun sync(): Completable {
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
  }

  private fun logError() = { e: Throwable ->
    val resolvedError = ErrorResolver.resolve(e)
    when (resolvedError) {
      is ResolvedError.Unexpected -> {
        Timber.i("(breadcrumb) Reporting to sentry. Error: $e. Resolved error: $resolvedError")
        crashReporter.report(resolvedError.actualCause)
      }
      is ResolvedError.NetworkRelated -> {
        // Connectivity issues are expected.
      }
    }.exhaustive()
    Timber.e(e)
  }
}
