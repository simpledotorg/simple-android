package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestData
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchResult.PatientNameAndId
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var repository: PatientRepository
  private lateinit var config: PatientConfig
  private val database = mock<AppDatabase>()

  private val patientSearchResultDao = mock<PatientSearchResult.RoomDao>()
  private val patientDao = mock<Patient.RoomDao>()
  private val patientAddressDao = mock<PatientAddress.RoomDao>()
  private val patientPhoneNumberDao = mock<PatientPhoneNumber.RoomDao>()
  private val bloodPressureMeasurementDao = mock<BloodPressureMeasurement.RoomDao>()
  private val businessIdDao = mock<BusinessId.RoomDao>()
  private val facilityRepository = mock<FacilityRepository>()
  private val searchPatientByName = mock<SearchPatientByName>()

  private val clock = TestUtcClock()
  private val dateOfBirthFormat = DateTimeFormatter.ISO_DATE
  private val user = TestData.loggedInUser()
  private val facility = TestData.facility()
  private val computationScheduler = TestScheduler()
  private val schedulersProvider = TestSchedulersProvider.trampoline(computationScheduler = computationScheduler)

  @Before
  fun setUp() {
    config = PatientConfig(limitOfSearchResults = 100, recentPatientLimit = 10)

    repository = PatientRepository(
        database = database,
        utcClock = clock,
        searchPatientByName = searchPatientByName,
        config = config,
        reportsRepository = mock(),
        businessIdMetaDataMoshiAdapter = mock(),
        dateOfBirthFormat = dateOfBirthFormat
    )

    whenever(facilityRepository.currentFacility()).thenReturn(Observable.just(facility))
    whenever(bloodPressureMeasurementDao.patientToFacilityIds(any())).thenReturn(emptyList())
    whenever(database.bloodPressureDao()).thenReturn(bloodPressureMeasurementDao)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }

  @Test
  @Parameters(method = "params for querying results for fuzzy search")
  fun `when the the filter patient by name returns results, the database must be queried for the complete information`(
      filteredUuids: List<UUID>,
      shouldQueryFilteredIds: Boolean
  ) {
    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(searchPatientByName.search(any(), any())).thenReturn(filteredUuids)
    whenever(patientSearchResultDao.searchByIds(any(), any()))
        .thenReturn(filteredUuids.map { TestData.patientSearchResult(uuid = it) })
    whenever(database.patientSearchDao().nameAndId(any())).thenReturn(emptyList())

    repository.search(Name("name"))

    if (shouldQueryFilteredIds) {
      verify(patientSearchResultDao, atLeastOnce()).searchByIds(filteredUuids, PatientStatus.Active)
    } else {
      verify(patientSearchResultDao, never()).searchByIds(filteredUuids, PatientStatus.Active)
    }
  }

  @Suppress("Unused")
  private fun `params for querying results for fuzzy search`(): List<List<Any>> {
    return listOf(
        listOf(listOf(UUID.randomUUID()), true),
        listOf(listOf(UUID.randomUUID(), UUID.randomUUID()), true),
        listOf(emptyList<UUID>(), false))
  }

  @Test
  @Parameters(method = "params for sorting results for fuzzy search")
  fun `when the filter patient by name returns results, the results must be sorted in the same order as the filtered ids`(
      filteredUuids: List<UUID>,
      results: List<PatientSearchResult>,
      expectedResults: List<PatientSearchResult>
  ) {
    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(searchPatientByName.search(any(), any())).thenReturn(filteredUuids)
    whenever(patientSearchResultDao.searchByIds(any(), any())).thenReturn(results)
    whenever(database.patientSearchDao().nameAndId(any())).thenReturn(emptyList())

    val actualResults = repository.search(Name("name"))
    assertThat(actualResults).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `params for sorting results for fuzzy search`(): List<List<Any>> {
    fun generateTestData(numberOfResults: Int): List<Any> {
      val filteredUuids = (1..numberOfResults).map { UUID.randomUUID() }
      val results = filteredUuids.map { TestData.patientSearchResult(uuid = it) }.shuffled()
      val expectedResults = filteredUuids.map { uuid -> results.find { it.uuid == uuid }!! }

      assertThat(results.map { it.uuid }).isNotEqualTo(filteredUuids)
      assertThat(expectedResults.map { it.uuid }).isEqualTo(filteredUuids)

      return listOf(filteredUuids, results, expectedResults)
    }

    return listOf(
        generateTestData(6),
        generateTestData(10))
  }

  @Test
  fun `the timing of all parts of search patient flow must be reported to analytics`() {
    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    val timeTakenToFetchPatientNameAndId = Duration.ofMinutes(1L)
    val timeTakenToFuzzyFilterPatientNames = Duration.ofMinutes(5L)
    val timeTakenToFetchPatientDetails = Duration.ofSeconds(45L)

    val patientUuid = UUID.randomUUID()

    whenever(patientSearchResultDao.nameAndId(any())).thenAnswer {
      clock.advanceBy(timeTakenToFetchPatientNameAndId)
      listOf(PatientNameAndId(patientUuid, "Name"))
    }
    whenever(searchPatientByName.search(any(), any())).thenAnswer {
      clock.advanceBy(timeTakenToFuzzyFilterPatientNames)
      listOf(patientUuid)
    }
    whenever(patientSearchResultDao.searchByIds(any(), any())).thenAnswer {
      clock.advanceBy(timeTakenToFetchPatientDetails)
      listOf(TestData.patientSearchResult(uuid = patientUuid))
    }
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)

    repository.search(Name("search"))

    val receivedEvents = reporter.receivedEvents
    assertThat(receivedEvents).hasSize(3)

    val (fetchNameAndId,
        fuzzyFilterByName,
        fetchPatientDetails) = receivedEvents

    assertThat(fetchNameAndId.props["operationName"]).isEqualTo("Search Patient:Fetch Name and Id")
    assertThat(fetchNameAndId.props["timeTakenInMillis"]).isEqualTo(timeTakenToFetchPatientNameAndId.toMillis())

    assertThat(fuzzyFilterByName.props["operationName"]).isEqualTo("Search Patient:Fuzzy Filtering By Name")
    assertThat(fuzzyFilterByName.props["timeTakenInMillis"]).isEqualTo(timeTakenToFuzzyFilterPatientNames.toMillis())

    assertThat(fetchPatientDetails.props["operationName"]).isEqualTo("Search Patient:Fetch Patient Details")
    assertThat(fetchPatientDetails.props["timeTakenInMillis"]).isEqualTo(timeTakenToFetchPatientDetails.toMillis())
  }
}
