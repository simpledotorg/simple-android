package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.fuzzy.AgeFuzzer
import org.simple.clinic.patient.fuzzy.BoundedAge
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.TestClock
import org.threeten.bp.LocalDate
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientRepositoryTest {

  private lateinit var repository: PatientRepository
  private lateinit var ageFuzzer: AgeFuzzer
  private lateinit var searchPatientByName: SearchPatientByName

  private val clock = TestClock()

  lateinit var database: AppDatabase
  lateinit var patientSearchResultDao: PatientSearchResult.RoomDao
  lateinit var patientDao: Patient.RoomDao
  lateinit var patientAddressDao: PatientAddress.RoomDao
  lateinit var patientPhoneNumberDao: PatientPhoneNumber.RoomDao
  lateinit var fuzzyPatientSearchDao: PatientFuzzySearch.PatientFuzzySearchDao
  lateinit var dobValidator: DateOfBirthFormatValidator
  lateinit var userSession: UserSession
  lateinit var facilityRepository: FacilityRepository
  lateinit var numberValidator: PhoneNumberValidator

  lateinit var config: PatientConfig

  @Before
  fun setUp() {
    config = PatientConfig(isFuzzySearchV2Enabled = false, limitOfSearchResults = 100)
    database = mock()
    patientSearchResultDao = mock()
    patientDao = mock()
    patientAddressDao = mock()
    patientPhoneNumberDao = mock()
    fuzzyPatientSearchDao = mock()
    dobValidator = mock()
    userSession = mock()
    facilityRepository = mock()
    numberValidator = mock()

    ageFuzzer = mock()
    whenever(ageFuzzer.bounded(any())).thenReturn(BoundedAge(LocalDate.now(clock), LocalDate.now(clock)))
    searchPatientByName = mock()

    repository = PatientRepository(
        database,
        dobValidator,
        facilityRepository,
        userSession,
        numberValidator,
        clock,
        ageFuzzer,
        searchPatientByName,
        Single.fromCallable { config })

    val user = PatientMocker.loggedInUser()
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(PatientMocker.facility()))

    val mockBloodPressureDao = mock<BloodPressureMeasurement.RoomDao>()
    whenever(mockBloodPressureDao.patientToFacilityIds(any())).thenReturn(Flowable.just(listOf()))
    whenever(database.bloodPressureDao()).thenReturn(mockBloodPressureDao)
  }

  @Test
  @Parameters(value = [
    "Name, Name, Name",
    "Name Surname, Name Surname, NameSurname",
    "Name Surname, Name   Surname , NameSurname",
    "Old Name, Name-Surname, NameSurname",
    "Name, Name.Middle-Surname, NameMiddleSurname"
  ])
  fun `when merging patients with server records, update the searchable name of the patient to the full name stripped of all spaces and punctuation`(
      localFullName: String,
      remoteFullName: String,
      expectedSearchableName: String
  ) {
    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)

    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid, fullName = localFullName, syncStatus = SyncStatus.DONE)
    whenever(patientDao.getOne(patientUuid)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(addressUuid).toPayload()
    val serverPatientWithoutPhone = PatientPayload(
        uuid = patientUuid,
        fullName = remoteFullName,
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = PatientStatus.ACTIVE,
        createdAt = mock(),
        updatedAt = mock(),
        address = serverAddress,
        phoneNumbers = null)

    repository.mergeWithLocalData(listOf(serverPatientWithoutPhone)).blockingAwait()

    verify(patientDao).save(argThat<List<Patient>> { first().searchableName == expectedSearchableName })
  }

  @Test
  fun `when searching for patients with age bound, strip the search query of any whitespace or punctuation`() {
    whenever(patientSearchResultDao.search(any(), any(), any(), any())).thenReturn(Flowable.just(emptyList()))
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())).thenReturn(Single.just(emptyList()))
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)

    repository.search("Name   Surname", 40).blockingFirst()

    verify(patientSearchResultDao).search(eq("NameSurname"), any(), any(), any())
  }

  @Test
  @Parameters(value = ["5", "10", "13", "45"])
  fun `when searching for patients with age bound, use fuzzy age always`(age: Int) {
    whenever(patientSearchResultDao.search(any(), any(), any(), any())).thenReturn(Flowable.just(emptyList()))
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())).thenReturn(Single.just(emptyList()))
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)

    repository.search("Name   Surname", age).blockingFirst()

    verify(ageFuzzer).bounded(age)
  }

  @Test
  fun `when the fuzzy patient search returns results, they must be at the head of the final results list`() {
    val patientSearchResultTemplate = PatientMocker.patientSearchResult()

    val actualResults = listOf(patientSearchResultTemplate.copy(uuid = UUID.randomUUID()), patientSearchResultTemplate.copy(UUID.randomUUID()))
    val fuzzyResults = listOf(patientSearchResultTemplate.copy(uuid = UUID.randomUUID()))
    whenever(patientSearchResultDao.search(any(), any(), any(), any())).thenReturn(Flowable.just(actualResults))
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())).thenReturn(Single.just(fuzzyResults))
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)

    repository.search(name = "test", assumedAge = 30)
        .firstOrError()
        .test()
        .assertValue(fuzzyResults + actualResults)
  }

  @Test
  fun `when the fuzzy patient search returns results, they must not contain any duplicates`() {
    val patientSearchResultTemplate = PatientMocker.patientSearchResult()
    val actualResults = listOf(patientSearchResultTemplate.copy(uuid = UUID.randomUUID()), patientSearchResultTemplate.copy(UUID.randomUUID()))
    val fuzzyResults = listOf(patientSearchResultTemplate.copy(uuid = UUID.randomUUID()), actualResults[0])

    val expected = listOf(fuzzyResults[0], fuzzyResults[1], actualResults[1])
    whenever(patientSearchResultDao.search(any(), any(), any(), any())).thenReturn(Flowable.just(actualResults))
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())).thenReturn(Single.just(fuzzyResults))
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)

    repository.search(name = "test", assumedAge = 30)
        .firstOrError()
        .test()
        .assertValue(expected)
  }

  @Test
  @Parameters(value = [
    "PENDING, false",
    "IN_FLIGHT, false",
    "INVALID, true",
    "DONE, true"])
  fun `when merging patients with server records, ignore records that already exist locally and are syncing or pending-sync`(
      syncStatusOfLocalCopy: SyncStatus,
      serverRecordExpectedToBeSaved: Boolean
  ) {
    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)

    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(patientDao.getOne(patientUuid)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(uuid = addressUuid).toPayload()
    val serverPatientWithoutPhone = PatientPayload(
        uuid = patientUuid,
        fullName = "name",
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = PatientStatus.ACTIVE,
        createdAt = mock(),
        updatedAt = mock(),
        address = serverAddress,
        phoneNumbers = null)

    repository.mergeWithLocalData(listOf(serverPatientWithoutPhone)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(patientDao).save(argThat<List<Patient>> { isNotEmpty() })
      verify(patientAddressDao).save(argThat<List<PatientAddress>> { isNotEmpty() })

    } else {
      verify(patientDao).save(argThat<List<Patient>> { isEmpty() })
      verify(patientAddressDao).save(argThat<List<PatientAddress>> { isEmpty() })
    }
  }

  @Test
  @Parameters(value = [
    "PENDING, false",
    "IN_FLIGHT, false",
    "INVALID, true",
    "DONE, true"])
  fun `that already exist locally and are syncing or pending-sync`(
      syncStatusOfLocalCopy: SyncStatus,
      serverRecordExpectedToBeSaved: Boolean
  ) {
    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)

    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(patientDao.getOne(patientUuid)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(uuid = addressUuid).toPayload()
    val serverPatientWithPhone = PatientPayload(
        uuid = patientUuid,
        fullName = "name",
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = PatientStatus.ACTIVE,
        createdAt = mock(),
        updatedAt = mock(),
        address = serverAddress,
        phoneNumbers = listOf(PatientPhoneNumberPayload(UUID.randomUUID(), "1232", mock(), false, mock(), mock())))

    repository.mergeWithLocalData(listOf(serverPatientWithPhone)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(patientAddressDao).save(argThat<List<PatientAddress>> { isNotEmpty() })
      verify(patientDao).save(argThat<List<Patient>> { isNotEmpty() })
      verify(patientPhoneNumberDao).save(argThat { isNotEmpty() })

    } else {
      verify(patientAddressDao).save(argThat<List<PatientAddress>> { isEmpty() })
      verify(patientDao).save(argThat<List<Patient>> { isEmpty() })
      verify(patientPhoneNumberDao).save(emptyList())
    }
  }

  @Test
  @Parameters(value = [
    "true",
    "false"
  ])
  fun `when the fuzzySearchV2 is enabled, searching for patient should use the new flow`(isFuzzySearchV2Enabled: Boolean) {
    config = config.copy(isFuzzySearchV2Enabled = isFuzzySearchV2Enabled)

    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(searchPatientByName.search(any(), any())).thenReturn(Single.just(emptyList()))
    whenever(patientSearchResultDao.searchByIds(any(), any())).thenReturn(Single.just(emptyList()))
    whenever(patientSearchResultDao.search(any(), any(), any(), any())).thenReturn(Flowable.just(emptyList()))
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())).thenReturn(Single.just(emptyList()))
    whenever(database.patientSearchDao().nameWithDobBounds(any(), any(), any())).thenReturn(Flowable.just(emptyList()))

    repository.search("name", 10).blockingFirst()

    if (isFuzzySearchV2Enabled) {
      verify(patientSearchResultDao, atLeastOnce()).nameWithDobBounds(any(), any(), any())
      verify(searchPatientByName, atLeastOnce()).search(any(), any())
      verify(fuzzyPatientSearchDao, never()).searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())
      verify(patientSearchResultDao, never()).search(any(), any(), any(), any())
    } else {
      verify(patientSearchResultDao, never()).nameWithDobBounds(any(), any(), any())
      verify(searchPatientByName, never()).search(any(), any())
      verify(fuzzyPatientSearchDao).searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())
      verify(patientSearchResultDao).search(any(), any(), any(), any())
    }
  }

  @Test
  @Parameters(method = "params for querying results for v2 fuzzy search")
  fun `when the fuzzySearchV2 is enabled and the filter by name returns results, the database must be queried for the complete information`(
      filteredUuids: List<UUID>,
      shouldQueryFilteredIds: Boolean
  ) {
    config = config.copy(isFuzzySearchV2Enabled = true)

    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(searchPatientByName.search(any(), any())).thenReturn(Single.just(filteredUuids))
    whenever(patientSearchResultDao.searchByIds(any(), any()))
        .thenReturn(Single.just(filteredUuids.map { PatientMocker.patientSearchResult(uuid = it) }))
    whenever(database.patientSearchDao().nameWithDobBounds(any(), any(), any())).thenReturn(Flowable.just(emptyList()))

    repository.search("name", 10).blockingFirst()

    if (shouldQueryFilteredIds) {
      verify(patientSearchResultDao, atLeastOnce()).searchByIds(filteredUuids, PatientStatus.ACTIVE)
    } else {
      verify(patientSearchResultDao, never()).searchByIds(filteredUuids, PatientStatus.ACTIVE)
    }
  }

  @Suppress("Unused")
  private fun `params for querying results for v2 fuzzy search`(): List<List<Any>> {
    return listOf(
        listOf(listOf(UUID.randomUUID()), true),
        listOf(listOf(UUID.randomUUID(), UUID.randomUUID()), true),
        listOf(emptyList<UUID>(), false))
  }

  @Test
  @Parameters(method = "params for sorting results for v2 fuzzy search")
  fun `when the fuzzySearchV2 is enabled and the filter by name returns results, the results must be sorted in the same order as the filtered ids`(
      filteredUuids: List<UUID>,
      results: List<PatientSearchResult>,
      expectedResults: List<PatientSearchResult>
  ) {
    config = config.copy(isFuzzySearchV2Enabled = true)

    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(searchPatientByName.search(any(), any())).thenReturn(Single.just(filteredUuids))
    whenever(patientSearchResultDao.searchByIds(any(), any())).thenReturn(Single.just(results))
    whenever(database.patientSearchDao().nameWithDobBounds(any(), any(), any())).thenReturn(Flowable.just(emptyList()))

    val actualResults = repository.search("name", 10).blockingFirst()

    assertThat(actualResults).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `params for sorting results for v2 fuzzy search`(): List<List<Any>> {
    fun generateTestData(numberOfResults: Int): List<Any> {
      val filteredUuids = (1..numberOfResults).map { UUID.randomUUID() }
      val results = filteredUuids.map { PatientMocker.patientSearchResult(uuid = it) }.shuffled()
      val expectedResults = filteredUuids.map { uuid -> results.find { it.uuid == uuid }!! }

      assertThat(results.map { it.uuid }).isNotEqualTo(filteredUuids)
      assertThat(expectedResults.map { it.uuid }).isEqualTo(filteredUuids)

      return listOf(filteredUuids, results, expectedResults)
    }

    return listOf(
        generateTestData(6),
        generateTestData(10))
  }
}
