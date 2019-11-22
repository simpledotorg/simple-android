package org.simple.clinic.user.clearpatientdata

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.security.pin.BruteForceProtection
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.threeten.bp.Duration

@RunWith(JUnitParamsRunner::class)
class SyncAndClearPatientDataTest {

  private val dataSync = mock<DataSync>()
  private val patientRepository = mock<PatientRepository>()
  private val userDao = mock<User.RoomDao>()
  private val bruteForceProtection = mock<BruteForceProtection>()
  private val patientPullToken = mock<Preference<Optional<String>>>()
  private val bpPullToken = mock<Preference<Optional<String>>>()
  private val appointmentPullToken = mock<Preference<Optional<String>>>()
  private val prescriptionPullToken = mock<Preference<Optional<String>>>()
  private val medicalHistoryPullToken = mock<Preference<Optional<String>>>()

  @Test
  fun `after clearing patient related data during forgot PIN flow, the sync timestamps must be cleared`() {
    whenever(dataSync.syncTheWorld()) doReturn Completable.complete()
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    var bruteForceReset = false
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.fromAction { bruteForceReset = true }

    createTestInstance().run().blockingAwait()

    verify(patientPullToken).delete()
    verify(bpPullToken).delete()
    verify(appointmentPullToken).delete()
    verify(medicalHistoryPullToken).delete()
    verify(prescriptionPullToken).delete()
    assertThat(bruteForceReset).isTrue()
  }

  @Test
  @Parameters(value = ["0", "1", "2"])
  fun `if the sync fails when resetting PIN, it should be retried and complete if all retries fail`(retryCount: Int) {
    val emissionsAfterFirst: Array<Completable> = (0 until retryCount)
        .map { Completable.error(RuntimeException()) }.toTypedArray()

    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(dataSync.syncTheWorld()).doReturn(Completable.error(RuntimeException()), *emissionsAfterFirst)
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance().run()
        .test()
        .await()
        .assertComplete()
  }

  @Test
  @Parameters(value = ["0", "1", "2"])
  fun `if the sync fails when resetting PIN, it should be retried and complete if any retry succeeds`(retryCount: Int) {
    // Mockito doesn't have a way to specify a vararg for all invocations and expects
    // the first emission to be explicitly provided. This dynamically constructs the
    // rest of the emissions and ensures that the last one succeeds.
    val emissionsAfterFirst: Array<Completable> = (0 until retryCount)
        .map { retryIndex ->
          if (retryIndex == retryCount - 1) Completable.complete() else Completable.error(RuntimeException())
        }.toTypedArray()

    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(dataSync.syncTheWorld()).doReturn(Completable.error(RuntimeException()), *emissionsAfterFirst)
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance().run()
        .test()
        .await()
        .assertComplete()
  }

  @Test
  fun `if the sync fails when resetting the PIN, it should clear the patient related data`() {
    whenever(dataSync.syncTheWorld()) doReturn Completable.complete()
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance().run()
        .test()
        .await()

    verify(patientRepository).clearPatientData()
  }

  @Test
  fun `if the sync succeeds when resetting the PIN, it should clear the patient related data`() {
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(dataSync.syncTheWorld()) doReturn Completable.complete()
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance().run()
        .test()
        .await()

    verify(patientRepository).clearPatientData()
  }

  @Test
  fun `when performing sync and clear data, the sync must be triggered`() {
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(dataSync.syncTheWorld()) doReturn Completable.complete()
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance().run()

    verify(dataSync).syncTheWorld()
  }

  private fun createTestInstance(): SyncAndClearPatientData {
    return SyncAndClearPatientData(
        dataSync = dataSync,
        bruteForceProtection = bruteForceProtection,
        patientRepository = patientRepository,
        schedulersProvider = TrampolineSchedulersProvider(),
        syncRetryCount = 0,
        syncTimeout = Duration.ofSeconds(1),
        patientSyncPullToken = patientPullToken,
        bpSyncPullToken = bpPullToken,
        prescriptionSyncPullToken = prescriptionPullToken,
        appointmentSyncPullToken = appointmentPullToken,
        medicalHistorySyncPullToken = medicalHistoryPullToken
    )
  }
}
