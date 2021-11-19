package org.simple.clinic.drugs.search

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.PagingTestCase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.protocol.ProtocolAndProtocolDrugs
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject

class DrugRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var drugRepository: DrugRepository

  @Inject
  lateinit var protocolRepository: ProtocolRepository

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(SaveDatabaseRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_drugs_should_work_correctly() {
    // given
    val drugs = listOf(
        TestData.drug(
            id = UUID.fromString("4e64d256-ea7e-4b90-8177-1eb1485630c3"),
            name = "Amlodipine",
            dosage = "10 mg"
        ),
        TestData.drug(
            id = UUID.fromString("0864635d-fd63-4005-9cea-ba843ea804e6"),
            name = "Amlodipine",
            dosage = "20 mg"
        )
    )

    // when
    drugRepository.save(drugs)

    // then
    val savedDrugs = drugRepository.drugs()

    assertThat(savedDrugs).isEqualTo(drugs)
  }

  @Test
  fun searching_for_drugs_should_return_drugs_that_are_not_in_protocol() {
    // given
    val amlodipine10 = TestData.drug(
        id = UUID.fromString("fd422777-d6dd-4afc-9fd1-8147e8578ca4"),
        name = "Amlodipine",
        dosage = "10mg",
        rxNormCode = "329528"
    )
    val amlodipine20 = TestData.drug(
        id = UUID.fromString("d48fd21f-41fd-4e37-9616-fa989964b945"),
        name = "Amlodipine",
        dosage = "20mg",
        rxNormCode = "329526"
    )
    val losartan25 = TestData.drug(
        id = UUID.fromString("5db739bb-1fa6-470c-b230-4fead4e92543"),
        name = "Losartan",
        dosage = "25mg",
        rxNormCode = "979484"
    )
    val drugs = listOf(amlodipine10, amlodipine20, losartan25)

    val obviousProtocol = TestData.protocol(uuid = UUID.fromString("7d26ecc1-707c-475e-b476-d7da6c4ad263"),
        name = "Protocol Obvious")

    val obviousProtocolDrugs = listOf(
        TestData.protocolDrug(
            uuid = UUID.fromString("19dfa8b3-9295-415d-9661-787b2a13ec99"),
            name = "Amlodipine",
            dosage = "20mg",
            rxNormCode = "329526",
            protocolUuid = obviousProtocol.uuid
        ),
        TestData.protocolDrug(
            uuid = UUID.fromString("eaf9b5e8-ecd1-4c11-af5d-4f6b54671d32"),
            name = "Metoprolol",
            dosage = "25mg",
            rxNormCode = "866426",
            protocolUuid = obviousProtocol.uuid
        )
    )

    val notObviousProtocol = TestData.protocol(uuid = UUID.fromString("6515a4cd-3b2f-40f2-abf0-be30285a461c"),
        name = "Protocol Not Obvious")

    val notObviousProtocolDrugs = listOf(
        TestData.protocolDrug(
            uuid = UUID.fromString("4b9a48f2-456a-4fcf-994d-f9bb90529ea0"),
            name = "Amlodipine",
            dosage = "10mg",
            rxNormCode = "329528",
            protocolUuid = notObviousProtocol.uuid
        ),
    )

    protocolRepository.save(listOf(
        ProtocolAndProtocolDrugs(obviousProtocol, obviousProtocolDrugs),
        ProtocolAndProtocolDrugs(notObviousProtocol, notObviousProtocolDrugs)
    ))
    drugRepository.save(drugs)

    // when
    val expectedSearchResultsForObviousProtocol = PagingTestCase(pagingSource = drugRepository.searchForNonProtocolDrugs(query = "amlo", protocolId = obviousProtocol.uuid),
        loadSize = 10)
        .loadPage()
        .data

    val expectedSearchResultsForNoProtocol = PagingTestCase(pagingSource = drugRepository.searchForNonProtocolDrugs(query = "amlo", protocolId = null),
        loadSize = 10)
        .loadPage()
        .data

    // then
    assertThat(expectedSearchResultsForObviousProtocol).containsExactly(amlodipine10).inOrder()
    assertThat(expectedSearchResultsForNoProtocol).containsExactly(amlodipine10, amlodipine20).inOrder()
  }

  @Test
  fun `getting_drug_immediately_should_work_correctly`() {
    // given
    val drugSearchUUID = UUID.fromString("0864635d-fd63-4005-9cea-ba843ea804e6")
    val searchedDrug = TestData.drug(
        id = drugSearchUUID,
        name = "Amlodipine",
        dosage = "20 mg"
    )
    val drugs = listOf(
        TestData.drug(
            id = UUID.fromString("4e64d256-ea7e-4b90-8177-1eb1485630c3"),
            name = "Amlodipine",
            dosage = "10 mg"
        ),
        searchedDrug
    )

    // when
    drugRepository.save(drugs)

    // then
    val drugImmediate = drugRepository.drugImmediate(drugSearchUUID)
    assertThat(drugImmediate).isEqualTo(searchedDrug)
  }
}
