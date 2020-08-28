package org.simple.clinic.protocolv2

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject


class ProtocolRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var protocolRepository: ProtocolRepository

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_protocols_are_not_present_in_database_then_default_drugs_should_be_returned() {
    database.clearAllTables()

    val randomProtocolUuid = UUID.randomUUID()
    val drugs = protocolRepository.drugsForProtocolOrDefault(randomProtocolUuid)
    assertThat(drugs).isEqualTo(protocolRepository.defaultProtocolDrugs())
  }

  @Test
  fun when_protocol_ID_is_null_then_default_drugs_should_be_returned() {
    val drugs = protocolRepository.drugsForProtocolOrDefault(null)
    assertThat(drugs).isEqualTo(protocolRepository.defaultProtocolDrugs())
  }

  @Test
  fun when_drugs_are_not_present_for_a_protocol_then_default_values_should_be_returned() {
    database.clearAllTables()

    val protocol1 = testData.protocol()
    val protocol2 = testData.protocol()
    database.protocolDao().save(listOf(protocol1, protocol2))

    val drug1 = testData.protocolDrug(protocolUuid = protocol1.uuid)
    val drug2 = testData.protocolDrug(protocolUuid = protocol1.uuid)
    database.protocolDrugDao().save(listOf(drug1, drug2))

    val drugsForProtocol2 = protocolRepository.drugsForProtocolOrDefault(protocol2.uuid)
    assertThat(drugsForProtocol2).isEqualTo(protocolRepository.defaultProtocolDrugs())
  }

  @Test
  fun when_protocols_are_present_in_database_then_they_should_be_returned() {
    database.clearAllTables()

    val currentProtocolUuid = UUID.randomUUID()

    val protocol1 = testData.protocol(uuid = currentProtocolUuid)
    val protocol2 = testData.protocol()
    database.protocolDao().save(listOf(protocol1, protocol2))

    val drug1 = testData.protocolDrug(name = "Amlodipine", protocolUuid = protocol1.uuid)
    val drug2 = testData.protocolDrug(name = "Telmisartan", protocolUuid = protocol1.uuid)
    val drug3 = testData.protocolDrug(name = "Amlodipine", protocolUuid = protocol2.uuid)
    database.protocolDrugDao().save(listOf(drug1, drug2, drug3))

    val drugsForCurrentProtocol = protocolRepository.drugsForProtocolOrDefault(currentProtocolUuid)
    assertThat(drugsForCurrentProtocol).containsAtLeast(
        ProtocolDrugAndDosages(drugName = "Amlodipine", drugs = listOf(drug1)),
        ProtocolDrugAndDosages(drugName = "Telmisartan", drugs = listOf(drug2)))
    assertThat(drugsForCurrentProtocol).doesNotContain(drug3)
    assertThat(drugsForCurrentProtocol).hasSize(2)
  }

  @Test
  fun protocols_drugs_should_be_grouped_by_names() {
    database.clearAllTables()

    val protocol1 = testData.protocol()
    val protocol2 = testData.protocol()
    val protocols = listOf(protocol1, protocol2)
    database.protocolDao().save(protocols)

    val amlodipine5mg = testData.protocolDrug(name = "Amlodipine", dosage = "5mg", protocolUuid = protocol1.uuid)
    val amlodipine10mg = testData.protocolDrug(name = "Amlodipine", dosage = "10mg", protocolUuid = protocol1.uuid)
    val telmisartan40mg = testData.protocolDrug(name = "Telmisartan", dosage = "40mg", protocolUuid = protocol1.uuid)
    val telmisartan80mg = testData.protocolDrug(name = "Telmisartan", dosage = "80mg", protocolUuid = protocol2.uuid)
    database.protocolDrugDao().save(listOf(amlodipine5mg, amlodipine10mg, telmisartan40mg, telmisartan80mg))

    val drugsForProtocol1 = protocolRepository.drugsForProtocolOrDefault(protocol1.uuid)
    assertThat(drugsForProtocol1).containsAtLeast(
        ProtocolDrugAndDosages(drugName = "Amlodipine", drugs = listOf(amlodipine5mg, amlodipine10mg)),
        ProtocolDrugAndDosages(drugName = "Telmisartan", drugs = listOf(telmisartan40mg)))
  }

  @Test
  fun protocol_drugs_received_in_an_order_should_be_returned_in_the_same_order() {
    database.clearAllTables()

    val protocolUuid = UUID.randomUUID()
    val drugPayload1 = testData.protocolDrugPayload(name = "Amlodipine", protocolUuid = protocolUuid, dosage = "5mg")
    val drugPayload2 = testData.protocolDrugPayload(name = "Telmisartan", protocolUuid = protocolUuid, dosage = "10mg")
    val drugPayload3 = testData.protocolDrugPayload(name = "Telmisartan", protocolUuid = protocolUuid, dosage = "20mg")
    val protocolPayload = testData.protocolPayload(uuid = protocolUuid, protocolDrugs = listOf(drugPayload1, drugPayload2, drugPayload3))

    val drug1 = drugPayload1.toDatabaseModel(order = 0)
    val drug2 = drugPayload2.toDatabaseModel(order = 1)
    val drug3 = drugPayload3.toDatabaseModel(order = 2)

    protocolRepository.mergeWithLocalData(listOf(protocolPayload))

    val drugsForProtocol = protocolRepository.drugsForProtocolOrDefault(protocolUuid)
    assertThat(drugsForProtocol).containsAtLeast(
        ProtocolDrugAndDosages(drugName = "Amlodipine", drugs = listOf(drug1)),
        ProtocolDrugAndDosages(drugName = "Telmisartan", drugs = listOf(drug2, drug3)))
  }

  @Test
  fun when_fetching_dosages_for_a_drug_then_only_the_dosages_for_that_drug_in_current_protocol_should_be_returned() {
    database.clearAllTables()

    val protocol1 = testData.protocol()
    val protocol2 = testData.protocol()
    val protocols = listOf(protocol1, protocol2)
    database.protocolDao().save(protocols)

    val amlodipine5mg = testData.protocolDrug(name = "Amlodipine", dosage = "5mg", protocolUuid = protocol1.uuid)
    val amlodipine10mg = testData.protocolDrug(name = "Amlodipine", dosage = "10mg", protocolUuid = protocol1.uuid)
    val amlodipine20mg = testData.protocolDrug(name = "Amlodipine", dosage = "20mg", protocolUuid = protocol2.uuid)
    val telmisartan40mg = testData.protocolDrug(name = "Telmisartan", dosage = "40mg", protocolUuid = protocol1.uuid)
    val telmisartan80mg = testData.protocolDrug(name = "Telmisartan", dosage = "80mg", protocolUuid = protocol2.uuid)
    database.protocolDrugDao().save(listOf(amlodipine5mg, amlodipine10mg, amlodipine20mg, telmisartan40mg, telmisartan80mg))

    val matchingDrugs = protocolRepository.drugsByNameOrDefault(drugName = "Amlodipine", protocolUuid = protocol1.uuid).blockingFirst()
    assertThat(matchingDrugs).isEqualTo(listOf(amlodipine5mg, amlodipine10mg))
  }

  @Test
  fun when_fetching_dosages_for_a_drug_and_protocol_id_is_null_then_default_dosages_should_be_returned() {
    val protocol = testData.protocol()
    database.protocolDao().save(listOf(protocol))

    val drugName = "Amlodipine"

    val amlodipine20mg = testData.protocolDrug(name = drugName, dosage = "20mg", protocolUuid = protocol.uuid)
    database.protocolDrugDao().save(listOf(amlodipine20mg))

    val defaultDrugs = protocolRepository.defaultProtocolDrugs()
        .filter { it.drugName == drugName }
        .flatMap { drugAndDosages -> drugAndDosages.drugs }

    val matchingDrugs = protocolRepository.drugsByNameOrDefault(drugName = drugName, protocolUuid = null).blockingFirst()

    assertThat(matchingDrugs).isEqualTo(defaultDrugs)
  }
}
