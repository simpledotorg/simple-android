package org.simple.clinic.encounter

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.util.RxErrorsRule
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject


class EncounterRepositoryAndroidTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: EncounterRepository

  @Inject
  lateinit var testData: TestData

  private val authenticationRule = LocalAuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun record_count_should_return_the_count_of_no_of_encounters() {
    //given
    repository.save(listOf(testData.observationsForEncounter(testData.encounter(
        uuid = UUID.fromString("6f70d92d-340e-42ea-8274-ccbf3b174d7c"),
        patientUuid = UUID.fromString("c25805f2-033b-45ad-bd75-45a2c3d29e87")
    )))).blockingAwait()

    //when
    val count = repository.recordCount().blockingFirst()

    //then
    assertThat(count).isEqualTo(1)
  }

  @Test
  fun encounter_payload_should_merge_correctly_with_encounter_database_model() {
    //given
    val patientUuid = UUID.fromString("c2273c6c-036b-41f0-a128-84d5551be3ce")
    val encounterUuid1 = UUID.fromString("0b144e4a-ce13-431d-93f1-1d10d0639c09")
    val encounterUuid2 = UUID.fromString("0799e067-6b14-4e45-8171-e6a9d84626fb")
    val bpUuid1 = UUID.fromString("0347a91f-8776-418d-a117-7cf6a4ae0713")
    val bpUuid2 = UUID.fromString("cd90ca58-b8ea-413c-bdb5-c5941b943f96")

    val bpPayloads1 = listOf(testData.bpPayload(uuid = bpUuid1, patientUuid = patientUuid))
    val bpPayloads2 = listOf(testData.bpPayload(uuid = bpUuid2, patientUuid = patientUuid))

    val encountersPayload1 = testData.encounterPayload(uuid = encounterUuid1, patientUuid = patientUuid, updatedAt = Instant.parse("2018-02-11T00:00:00Z"), bpPayloads = bpPayloads1)
    val encountersPayload2 = testData.encounterPayload(uuid = encounterUuid2, patientUuid = patientUuid, bpPayloads = bpPayloads2)

    val encounter1 = testData.encounter(
        uuid = encounterUuid1,
        patientUuid = patientUuid,
        syncStatus = SyncStatus.PENDING,
        updatedAt = Instant.parse("2018-02-13T00:00:00Z")
    )
    repository.save(listOf(testData.observationsForEncounter(
        encounter = encounter1
    ))).blockingAwait()

    //when
    repository.mergeWithLocalData(listOf(encountersPayload1, encountersPayload2)).blockingAwait()
    val observationsForEncounters = repository.recordsWithSyncStatus(SyncStatus.DONE).blockingGet()
    val pendingObservationsForEncounters = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()

    //then
    assertThat(observationsForEncounters.size).isEqualTo(1)
    with(observationsForEncounters.first()) {
      assertThat(encounter.uuid).isEqualTo(encounterUuid2)
      assertThat(bloodPressures.size).isEqualTo(1)
      assertThat(bloodPressures.first().uuid).isEqualTo(bpUuid2)
    }
    assertThat(pendingObservationsForEncounters.size).isEqualTo(1)
    with(pendingObservationsForEncounters.first().encounter) {
      assertThat(uuid).isEqualTo(encounterUuid1)
      assertThat(updatedAt).isEqualTo(Instant.parse("2018-02-13T00:00:00Z"))
    }
  }
}
