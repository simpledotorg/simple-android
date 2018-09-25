package org.simple.clinic.medicalhistory

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit.DAYS
import java.util.UUID
import javax.inject.Inject

class MedicalHistoryRepositoryAndroidTest {

  @Inject
  lateinit var repository: MedicalHistoryRepository

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var clock: Clock

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_creating_new_medical_history_then_the_medical_history_should_be_saved() {
    val patientUuid = UUID.randomUUID()
    val historyEntry = OngoingMedicalHistoryEntry(
        hasHadHeartAttack = true,
        hasHadStroke = true,
        hasHadKidneyDisease = true,
        isOnTreatmentForHypertension = false,
        hasDiabetes = false)

    repository.save(patientUuid, historyEntry).blockingAwait()

    val savedHistory = repository.historyForPatient(patientUuid).blockingFirst()

    assertThat(savedHistory.hasHadHeartAttack).isTrue()
    assertThat(savedHistory.hasHadStroke).isTrue()
    assertThat(savedHistory.hasHadKidneyDisease).isTrue()
    assertThat(savedHistory.isOnTreatmentForHypertension).isFalse()
    assertThat(savedHistory.hasDiabetes).isFalse()
    assertThat(savedHistory.syncStatus).isEqualTo(SyncStatus.PENDING)
  }

  @Test
  fun when_updating_an_existing_medical_history_then_it_should_be_marked_as_pending_sync() {
    val patientUuid = UUID.randomUUID()
    val oldHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        hasHadHeartAttack = false,
        syncStatus = SyncStatus.DONE,
        updatedAt = Instant.now().minus(10, DAYS))

    repository.save(listOf(oldHistory)).blockingAwait()

    val newHistory = oldHistory.copy(hasHadHeartAttack = true)
    repository.update(newHistory).blockingAwait()

    val updatedHistory = repository.historyForPatient(patientUuid).blockingFirst()

    assertThat(updatedHistory.hasHadHeartAttack).isTrue()
    assertThat(updatedHistory.syncStatus).isEqualTo(SyncStatus.PENDING)
    assertThat(updatedHistory.updatedAt).isEqualTo(clock.instant())
  }
}
