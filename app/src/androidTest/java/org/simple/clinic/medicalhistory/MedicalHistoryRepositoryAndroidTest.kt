package org.simple.clinic.medicalhistory

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.NO
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.UNSELECTED
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.YES
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
  lateinit var dao: MedicalHistory.RoomDao

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var clock: Clock

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    dao.clear()
  }

  @Test
  fun when_creating_new_medical_history_from_ongoing_entry_then_the_medical_history_should_be_saved() {
    val patientUuid = UUID.randomUUID()
    val historyEntry = OngoingMedicalHistoryEntry(
        hasHadHeartAttack = YES,
        hasHadStroke = YES,
        hasHadKidneyDisease = YES,
        isOnTreatmentForHypertension = NO,
        hasDiabetes = NO)

    repository.save(patientUuid, historyEntry).blockingAwait()

    val savedHistory = repository.historyForPatientOrDefault(patientUuid).blockingFirst()

    assertThat(savedHistory.hasHadHeartAttack).isEqualTo(YES)
    assertThat(savedHistory.hasHadStroke).isEqualTo(YES)
    assertThat(savedHistory.hasHadKidneyDisease).isEqualTo(YES)
    assertThat(savedHistory.isOnTreatmentForHypertension).isEqualTo(NO)
    assertThat(savedHistory.hasDiabetes).isEqualTo(NO)
    assertThat(savedHistory.syncStatus).isEqualTo(SyncStatus.PENDING)
  }

  @Test
  fun when_creating_new_medical_history_then_the_medical_history_should_be_saved() {
    val patientUuid = UUID.randomUUID()
    val historyToSave = testData.medicalHistory(patientUuid = patientUuid)

    repository.save(historyToSave).blockingAwait()

    val savedHistory = repository.historyForPatientOrDefault(patientUuid).blockingFirst()
    val expectedSavedHistory = historyToSave.copy(syncStatus = SyncStatus.PENDING, updatedAt = Instant.now(clock))

    assertThat(savedHistory).isEqualTo(expectedSavedHistory)
  }

  @Test
  fun when_updating_an_existing_medical_history_then_it_should_be_marked_as_pending_sync() {
    val patientUuid = UUID.randomUUID()
    val oldHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        hasHadHeartAttack = NO,
        syncStatus = SyncStatus.DONE,
        updatedAt = Instant.now().minus(10, DAYS))

    repository.save(listOf(oldHistory)).blockingAwait()

    val newHistory = oldHistory.copy(hasHadHeartAttack = YES)
    repository.save(newHistory).blockingAwait()

    val updatedHistory = repository.historyForPatientOrDefault(patientUuid).blockingFirst()

    assertThat(updatedHistory.hasHadHeartAttack).isEqualTo(YES)
    assertThat(updatedHistory.syncStatus).isEqualTo(SyncStatus.PENDING)
    assertThat(updatedHistory.updatedAt).isEqualTo(clock.instant())
  }

  @Test
  fun when_medical_history_isnt_present_for_a_patient_then_an_empty_value_should_be_returned() {
    val emptyHistory = repository.historyForPatientOrDefault(UUID.randomUUID()).blockingFirst()

    assertThat(emptyHistory.hasHadHeartAttack).isEqualTo(UNSELECTED)
    assertThat(emptyHistory.hasHadStroke).isEqualTo(UNSELECTED)
    assertThat(emptyHistory.hasHadKidneyDisease).isEqualTo(UNSELECTED)
    assertThat(emptyHistory.isOnTreatmentForHypertension).isEqualTo(UNSELECTED)
    assertThat(emptyHistory.hasDiabetes).isEqualTo(UNSELECTED)
    assertThat(emptyHistory.syncStatus).isEqualTo(SyncStatus.DONE)
  }

  @Test
  fun when_multiple_medical_histories_are_present_for_a_patient_then_only_the_last_edited_one_should_be_returned() {
    val patientUuid = UUID.randomUUID()

    val olderHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minusMillis(100),
        updatedAt = Instant.now(clock).minusMillis(100))

    val newerHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minusMillis(100),
        updatedAt = Instant.now(clock))

    repository.save(olderHistory, updateTime = {olderHistory.updatedAt})
        .andThen(repository.save(newerHistory, updateTime = {newerHistory.updatedAt}))
        .blockingAwait()

    val foundHistory = repository.historyForPatientOrDefault(patientUuid).blockingFirst()
    assertThat(foundHistory.uuid).isEqualTo(newerHistory.uuid)
  }
}
