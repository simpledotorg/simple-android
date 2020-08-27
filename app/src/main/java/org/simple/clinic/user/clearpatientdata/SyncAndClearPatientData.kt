package org.simple.clinic.user.clearpatientdata

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.security.pin.BruteForceProtection
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.Optional
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class SyncAndClearPatientData @Inject constructor(
    private val dataSync: DataSync,
    private val bruteForceProtection: BruteForceProtection,
    private val patientRepository: PatientRepository,
    @Named("clear_patient_data_sync_retry_count") private val syncRetryCount: Int,
    @Named("clear_patient_data_sync_timeout") private val syncTimeout: Duration,
    @Named("last_patient_pull_token") private val patientSyncPullToken: Preference<Optional<String>>,
    @Named("last_bp_pull_token") private val bpSyncPullToken: Preference<Optional<String>>,
    @Named("last_prescription_pull_token") private val prescriptionSyncPullToken: Preference<Optional<String>>,
    @Named("last_appointment_pull_token") private val appointmentSyncPullToken: Preference<Optional<String>>,
    @Named("last_medicalhistory_pull_token") private val medicalHistorySyncPullToken: Preference<Optional<String>>,
    @Named("last_blood_sugar_pull_token") private val bloodSugarSyncPullToken: Preference<Optional<String>>
) {

  fun run(): Completable {
    Timber.i("Syncing and clearing all patient related data")

    return dataSync
        .syncTheWorld()
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
      bloodSugarSyncPullToken.delete()
    }
  }
}
