package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.atLeastOnce
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var repository: PatientRepository
  private lateinit var searchPatientByName: SearchPatientByName
  private lateinit var database: AppDatabase
  private lateinit var patientSearchResultDao: PatientSearchResult.RoomDao
  private lateinit var patientDao: Patient.RoomDao
  private lateinit var patientAddressDao: PatientAddress.RoomDao
  private lateinit var patientPhoneNumberDao: PatientPhoneNumber.RoomDao
  private lateinit var fuzzyPatientSearchDao: PatientFuzzySearch.PatientFuzzySearchDao
  private lateinit var dobValidator: UserInputDateValidator
  private lateinit var userSession: UserSession
  private lateinit var facilityRepository: FacilityRepository
  private lateinit var numberValidator: PhoneNumberValidator
  private lateinit var config: PatientConfig

  private val clock = TestClock()
  private val dateOfBirthFormat = DateTimeFormatter.ISO_DATE

  @Before
  fun setUp() {
    config = PatientConfig(limitOfSearchResults = 100)
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
    searchPatientByName = mock()

    repository = PatientRepository(
        database = database,
        dobValidator = dobValidator,
        facilityRepository = facilityRepository,
        userSession = userSession,
        numberValidator = numberValidator,
        clock = clock,
        dateOfBirthFormat = dateOfBirthFormat,
        searchPatientByName = searchPatientByName,
        configProvider = Single.fromCallable { config },
        reportsRepository = mock())

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
        phoneNumbers = null,
        deletedAt = null)

    repository.mergeWithLocalData(listOf(serverPatientWithoutPhone)).blockingAwait()

    verify(patientDao).save(argThat<List<Patient>> { first().searchableName == expectedSearchableName })
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
        phoneNumbers = null,
        deletedAt = null)

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
        phoneNumbers = listOf(PatientPhoneNumberPayload(
            uuid = UUID.randomUUID(),
            number = "1232",
            type = mock(),
            active = false,
            createdAt = mock(),
            updatedAt = mock(),
            deletedAt = mock())),
        deletedAt = null)

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
  @Parameters(method = "params for querying results for fuzzy search")
  fun `when the the filter patient by name returns results, the database must be queried for the complete information`(
      filteredUuids: List<UUID>,
      shouldQueryFilteredIds: Boolean
  ) {
    whenever(database.patientDao()).thenReturn(patientDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(patientPhoneNumberDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(searchPatientByName.search(any(), any())).thenReturn(Single.just(filteredUuids))
    whenever(patientSearchResultDao.searchByIds(any(), any()))
        .thenReturn(Single.just(filteredUuids.map { PatientMocker.patientSearchResult(uuid = it) }))
    whenever(database.patientSearchDao().nameAndId(any())).thenReturn(Flowable.just(emptyList()))

    repository.search("name").blockingFirst()

    if (shouldQueryFilteredIds) {
      verify(patientSearchResultDao, atLeastOnce()).searchByIds(filteredUuids, PatientStatus.ACTIVE)
    } else {
      verify(patientSearchResultDao, never()).searchByIds(filteredUuids, PatientStatus.ACTIVE)
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
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(searchPatientByName.search(any(), any())).thenReturn(Single.just(filteredUuids))
    whenever(patientSearchResultDao.searchByIds(any(), any())).thenReturn(Single.just(results))
    whenever(database.patientSearchDao().nameAndId(any())).thenReturn(Flowable.just(emptyList()))

    val actualResults = repository.search("name").blockingFirst()

    assertThat(actualResults).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `params for sorting results for fuzzy search`(): List<List<Any>> {
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
