package org.simple.clinic.medicalhistory

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Duration
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
  lateinit var clock: UtcClock

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

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
        hasHadHeartAttack = Yes,
        hasHadStroke = Yes,
        hasHadKidneyDisease = Yes,
        isOnTreatmentForHypertension = No,
        hasDiabetes = No)

    repository.save(patientUuid, historyEntry).blockingAwait()

    val savedHistory = repository.historyForPatientOrDefault(patientUuid).blockingFirst()

    assertThat(savedHistory.hasHadHeartAttack).isEqualTo(Yes)
    assertThat(savedHistory.hasHadStroke).isEqualTo(Yes)
    assertThat(savedHistory.hasHadKidneyDisease).isEqualTo(Yes)
    assertThat(savedHistory.isOnTreatmentForHypertension).isEqualTo(No)
    assertThat(savedHistory.hasDiabetes).isEqualTo(No)
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
        hasHadHeartAttack = No,
        syncStatus = SyncStatus.DONE,
        updatedAt = Instant.now().minus(10, DAYS))

    repository.save(listOf(oldHistory)).blockingAwait()

    val newHistory = oldHistory.copy(hasHadHeartAttack = Yes)
    repository.save(newHistory).blockingAwait()

    val updatedHistory = repository.historyForPatientOrDefault(patientUuid).blockingFirst()

    assertThat(updatedHistory.hasHadHeartAttack).isEqualTo(Yes)
    assertThat(updatedHistory.syncStatus).isEqualTo(SyncStatus.PENDING)
    assertThat(updatedHistory.updatedAt).isEqualTo(clock.instant())
  }

  @Test
  fun when_medical_history_isnt_present_for_a_patient_then_an_empty_value_should_be_returned() {
    val emptyHistory = repository.historyForPatientOrDefault(UUID.randomUUID()).blockingFirst()

    assertThat(emptyHistory.hasHadHeartAttack).isEqualTo(Unanswered)
    assertThat(emptyHistory.hasHadStroke).isEqualTo(Unanswered)
    assertThat(emptyHistory.hasHadKidneyDisease).isEqualTo(Unanswered)
    assertThat(emptyHistory.isOnTreatmentForHypertension).isEqualTo(Unanswered)
    assertThat(emptyHistory.hasDiabetes).isEqualTo(Unanswered)
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

    repository.save(olderHistory, updateTime = { olderHistory.updatedAt })
        .andThen(repository.save(newerHistory, updateTime = { newerHistory.updatedAt }))
        .blockingAwait()

    val foundHistory = repository.historyForPatientOrDefault(patientUuid).blockingFirst()
    assertThat(foundHistory.uuid).isEqualTo(newerHistory.uuid)
  }

  @Test
  fun querying_whether_medical_history_for_patient_has_changed_should_work_as_expected() {
    fun hasMedicalHistoryForPatientChangedSince(patientUuid: UUID, since: Instant): Boolean {
      return repository.hasMedicalHistoryForPatientChangedSince(patientUuid, since).blockingFirst()
    }

    fun setMedicalHistorySyncStatusToDone(medicalHistoryUuid: UUID) {
      repository.setSyncStatus(listOf(medicalHistoryUuid), SyncStatus.DONE).blockingAwait()
    }

    val patientUuid = UUID.randomUUID()
    val now = Instant.now(clock)
    val oneSecondEarlier = now.minus(Duration.ofSeconds(1))
    val fiftyNineSecondsLater = now.plus(Duration.ofSeconds(59))
    val oneMinuteLater = now.plus(Duration.ofMinutes(1))

    val medicalHistory1ForPatient = testData.medicalHistory(
        patientUuid = patientUuid,
        syncStatus = SyncStatus.PENDING,
        updatedAt = now
    )
    val medicalHistory2ForPatient = testData.medicalHistory(
        patientUuid = patientUuid,
        syncStatus = SyncStatus.PENDING,
        updatedAt = oneMinuteLater
    )
    val medicalHistoryForSomeOtherPatient = testData.medicalHistory(
        patientUuid = UUID.randomUUID(),
        syncStatus = SyncStatus.PENDING,
        updatedAt = now
    )

    repository.save(listOf(medicalHistory1ForPatient, medicalHistory2ForPatient, medicalHistoryForSomeOtherPatient)).blockingAwait()
    assertThat(hasMedicalHistoryForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()
    assertThat(hasMedicalHistoryForPatientChangedSince(patientUuid, now)).isTrue()
    assertThat(hasMedicalHistoryForPatientChangedSince(patientUuid, fiftyNineSecondsLater)).isTrue()
    assertThat(hasMedicalHistoryForPatientChangedSince(patientUuid, oneMinuteLater)).isFalse()

    setMedicalHistorySyncStatusToDone(medicalHistory2ForPatient.uuid)
    assertThat(hasMedicalHistoryForPatientChangedSince(patientUuid, fiftyNineSecondsLater)).isFalse()
    assertThat(hasMedicalHistoryForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()

    setMedicalHistorySyncStatusToDone(medicalHistory1ForPatient.uuid)
    assertThat(hasMedicalHistoryForPatientChangedSince(patientUuid, oneSecondEarlier)).isFalse()
    assertThat(hasMedicalHistoryForPatientChangedSince(medicalHistoryForSomeOtherPatient.patientUuid, oneSecondEarlier)).isTrue()

    setMedicalHistorySyncStatusToDone(medicalHistoryForSomeOtherPatient.uuid)
    assertThat(hasMedicalHistoryForPatientChangedSince(medicalHistoryForSomeOtherPatient.patientUuid, oneSecondEarlier)).isFalse()
  }
}
