package org.simple.clinic.medicalhistory

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.util.Rules
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
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
  val rules: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())

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
    val patientUuid = UUID.fromString("9a39d73b-6568-4359-90a7-1553d8cfa05c")
    val historyEntry = OngoingMedicalHistoryEntry(
        hasHadHeartAttack = Yes,
        hasHadStroke = Yes,
        hasHadKidneyDisease = Yes,
        hasDiabetes = No)

    repository.save(
        uuid = UUID.fromString("d33a3dfc-3da9-43a9-a543-095232c55597"),
        patientUuid = patientUuid,
        historyEntry = historyEntry
    ).blockingAwait()

    val savedHistory = dao.historyForPatientImmediate(patientUuid)!!

    assertThat(savedHistory.hasHadHeartAttack).isEqualTo(Yes)
    assertThat(savedHistory.hasHadStroke).isEqualTo(Yes)
    assertThat(savedHistory.hasHadKidneyDisease).isEqualTo(Yes)
    assertThat(savedHistory.diagnosedWithDiabetes).isEqualTo(No)
    assertThat(savedHistory.syncStatus).isEqualTo(SyncStatus.PENDING)
  }

  @Test
  fun when_creating_new_medical_history_then_the_medical_history_should_be_saved() {
    val patientUuid = UUID.fromString("89d8e57c-e895-4b65-b5c8-f69886d70f2e")
    val historyToSave = testData.medicalHistory(patientUuid = patientUuid)

    val instant = Instant.now(clock)
    repository.save(historyToSave, instant)

    val savedHistory = dao.historyForPatientImmediate(patientUuid)!!
    val expectedSavedHistory = historyToSave.copy(syncStatus = SyncStatus.PENDING, updatedAt = instant)

    assertThat(savedHistory).isEqualTo(expectedSavedHistory)
  }

  @Test
  fun when_updating_an_existing_medical_history_then_it_should_be_marked_as_pending_sync() {
    val patientUuid = UUID.fromString("93319278-e5bd-4adc-9627-7328c21a0bd3")
    val now = Instant.now(clock)
    val oldHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        hasHadHeartAttack = No,
        syncStatus = SyncStatus.DONE,
        updatedAt = now.minus(10, DAYS))

    repository.save(listOf(oldHistory)).blockingAwait()

    val newHistory = oldHistory.copy(hasHadHeartAttack = Yes)
    repository.save(newHistory, now)

    val updatedHistory = dao.historyForPatientImmediate(patientUuid)!!

    assertThat(updatedHistory.hasHadHeartAttack).isEqualTo(Yes)
    assertThat(updatedHistory.syncStatus).isEqualTo(SyncStatus.PENDING)
    assertThat(updatedHistory.updatedAt).isEqualTo(now)
  }

  @Test
  fun when_medical_history_is_not_present_for_a_patient_then_an_empty_value_should_be_returned() {
    val emptyHistoryUuid = UUID.fromString("b025b4e9-3907-4168-abd4-ab117739756d")
    val emptyHistory = repository.historyForPatientOrDefault(
        defaultHistoryUuid = emptyHistoryUuid,
        patientUuid = UUID.fromString("2a0ae00f-bf18-4710-9a33-df4be6e1846b")
    ).blockingFirst()

    assertThat(emptyHistory.uuid).isEqualTo(emptyHistoryUuid)
    assertThat(emptyHistory.hasHadHeartAttack).isEqualTo(Unanswered)
    assertThat(emptyHistory.hasHadStroke).isEqualTo(Unanswered)
    assertThat(emptyHistory.hasHadKidneyDisease).isEqualTo(Unanswered)
    assertThat(emptyHistory.diagnosedWithDiabetes).isEqualTo(Unanswered)
    assertThat(emptyHistory.syncStatus).isEqualTo(SyncStatus.DONE)
  }

  @Test
  fun when_multiple_medical_histories_are_present_for_a_patient_then_only_the_last_edited_one_should_be_returned() {
    val patientUuid = UUID.fromString("780a880d-0b00-441e-ad84-fe8b3d973ffd")

    val olderHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minusMillis(100),
        updatedAt = Instant.now(clock).minusMillis(100))

    val newerHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minusMillis(100),
        updatedAt = Instant.now(clock))

    repository.save(olderHistory, olderHistory.updatedAt)
    repository.save(newerHistory, newerHistory.updatedAt)

    val foundHistory = dao.historyForPatientImmediate(patientUuid)!!
    assertThat(foundHistory.uuid).isEqualTo(newerHistory.uuid)
  }

  @Test
  fun when_multiple_medical_histories_are_present_for_a_patient_then_only_the_last_edited_one_should_be_returned_immediately() {
    val patientUuid = UUID.fromString("44801303-cea8-40a2-a4c5-3e410ab8fabb")

    val olderHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minusMillis(100),
        updatedAt = Instant.now(clock).minusMillis(100))

    val newerHistory = testData.medicalHistory(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minusMillis(100),
        updatedAt = Instant.now(clock))

    dao.saveHistories(listOf(olderHistory, newerHistory))

    val foundHistory = dao.historyForPatientImmediate(patientUuid)!!
    assertThat(foundHistory).isEqualTo(newerHistory)
  }

  @Test
  fun when_no_medical_history_is_present_for_a_patient_then_return_detail_medical_history() {
    val emptyHistoryUuid = UUID.fromString("3f7b9090-daa9-4406-834f-563219fec5a3")
    val patientUuid = UUID.fromString("694d1c32-048f-4d43-93d4-0cd51be686b0")
    val emptyHistory = repository.historyForPatientOrDefaultImmediate(
        defaultHistoryUuid = emptyHistoryUuid,
        patientUuid = patientUuid
    )

    assertThat(emptyHistory.uuid).isEqualTo(emptyHistoryUuid)
    assertThat(emptyHistory.hasHadHeartAttack).isEqualTo(Unanswered)
    assertThat(emptyHistory.hasHadStroke).isEqualTo(Unanswered)
    assertThat(emptyHistory.hasHadKidneyDisease).isEqualTo(Unanswered)
    assertThat(emptyHistory.diagnosedWithHypertension).isEqualTo(Unanswered)
    assertThat(emptyHistory.diagnosedWithDiabetes).isEqualTo(Unanswered)
    assertThat(emptyHistory.syncStatus).isEqualTo(SyncStatus.DONE)
  }
}
