package org.simple.clinic.user.clearpatientdata

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.security.pin.BruteForceProtection
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.Duration
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncAndClearPatientData(
    private val dataSync: DataSync,
    private val syncRetryCount: Int,
    private val syncTimeout: Duration,
    private val bruteForceProtection: BruteForceProtection,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    private val patientSyncPullToken: Preference<Optional<String>>,
    private val bpSyncPullToken: Preference<Optional<String>>,
    private val prescriptionSyncPullToken: Preference<Optional<String>>,
    private val appointmentSyncPullToken: Preference<Optional<String>>,
    private val medicalHistorySyncPullToken: Preference<Optional<String>>
) {

  fun run(): Completable {
    Timber.i("Syncing and clearing all patient related data")

    return dataSync
        .syncTheWorld()
        .subscribeOn(schedulersProvider.io())
        .retry(syncRetryCount.toLong())
        .timeout(syncTimeout.seconds, TimeUnit.SECONDS)
        .onErrorComplete()
        .andThen(patientRepository.clearPatientData())
        .andThen(clearStoredPullTokens())
        .andThen(bruteForceProtection.resetFailedAttempts())
  }

  private fun clearStoredPullTokens(): Completable {
    return Completable.fromAction {
      patientSyncPullToken.delete()
      bpSyncPullToken.delete()
      prescriptionSyncPullToken.delete()
      appointmentSyncPullToken.delete()
      medicalHistorySyncPullToken.delete()
    }
  }
}
