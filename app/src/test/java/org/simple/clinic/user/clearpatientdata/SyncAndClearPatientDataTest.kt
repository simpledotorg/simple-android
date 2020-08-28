package org.simple.clinic.user.clearpatientdata

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.security.pin.BruteForceProtection
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import java.time.Duration
import java.io.IOException
import java.net.SocketTimeoutException

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
  private val bloodSugarSyncPullToken = mock<Preference<Optional<String>>>()

  @Test
  fun `after clearing patient related data during forgot PIN flow, the sync timestamps must be cleared`() {
    whenever(dataSync.syncTheWorld()) doReturn Completable.complete()
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()

    val user = TestData.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    var bruteForceReset = false
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.fromAction { bruteForceReset = true }

    createTestInstance().run().blockingAwait()

    verify(patientPullToken).delete()
    verify(bpPullToken).delete()
    verify(appointmentPullToken).delete()
    verify(medicalHistoryPullToken).delete()
    verify(prescriptionPullToken).delete()
    verify(bloodSugarSyncPullToken).delete()
    assertThat(bruteForceReset).isTrue()
  }

  @Test
  fun `if the sync fails when resetting PIN, it should be retried and complete if all retries fail`() {
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(dataSync.syncTheWorld()).doReturn(
        Completable.error(RuntimeException()),
        Completable.error(IOException()),
        Completable.error(SocketTimeoutException())
    )
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = TestData.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance(syncRetryCount = 2)
        .run()
        .test()
        .await()
        .assertComplete()
  }

  @Test
  fun `if the sync fails when resetting PIN, it should be retried and complete if any retry succeeds`() {
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(dataSync.syncTheWorld()).doReturn(
        Completable.error(RuntimeException()),
        Completable.error(IOException()),
        Completable.complete()
    )
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = TestData.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance(syncRetryCount = 2)
        .run()
        .test()
        .await()
        .assertComplete()
  }

  @Test
  fun `if the sync fails when resetting the PIN, it should clear the patient related data`() {
    whenever(dataSync.syncTheWorld()) doReturn Completable.complete()
    whenever(patientRepository.clearPatientData()) doReturn Completable.complete()
    whenever(bruteForceProtection.resetFailedAttempts()) doReturn Completable.complete()

    val user = TestData.loggedInUser()
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

    val user = TestData.loggedInUser()
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

    val user = TestData.loggedInUser()
    whenever(userDao.user()) doReturn Flowable.just(listOf(user))

    createTestInstance().run()

    verify(dataSync).syncTheWorld()
  }

  private fun createTestInstance(
      syncRetryCount: Int = 0
  ): SyncAndClearPatientData {
    return SyncAndClearPatientData(
        dataSync = dataSync,
        bruteForceProtection = bruteForceProtection,
        patientRepository = patientRepository,
        syncRetryCount = syncRetryCount,
        syncTimeout = Duration.ofSeconds(1),
        patientSyncPullToken = patientPullToken,
        bpSyncPullToken = bpPullToken,
        prescriptionSyncPullToken = prescriptionPullToken,
        appointmentSyncPullToken = appointmentPullToken,
        medicalHistorySyncPullToken = medicalHistoryPullToken,
        bloodSugarSyncPullToken = bloodSugarSyncPullToken
    )
  }
}
