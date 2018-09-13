package org.simple.clinic.patient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
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
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.user.UserSession
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientRepositoryTest {

  private lateinit var repository: PatientRepository
  private lateinit var database: AppDatabase
  private lateinit var mockPatientSearchResultDao: PatientSearchResult.RoomDao

  private val mockPatientDao = mock<Patient.RoomDao>()
  private val mockPatientAddressDao = mock<PatientAddress.RoomDao>()
  private val mockPatientPhoneNumberDao = mock<PatientPhoneNumber.RoomDao>()
  private val mockFuzzyPatientSearchDao = mock<PatientFuzzySearch.PatientFuzzySearchDao>()
  private val dobValidator = mock<DateOfBirthFormatValidator>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()

  @Before
  fun setUp() {
    database = mock()
    mockPatientSearchResultDao = mock()
    repository = PatientRepository(database, dobValidator, facilityRepository, userSession)

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
    whenever(database.patientDao()).thenReturn(mockPatientDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(mockFuzzyPatientSearchDao)
    whenever(mockFuzzyPatientSearchDao.updateTableForPatients(any())).thenReturn(Completable.complete())

    val patientUUID = UUID.randomUUID()
    val addressUUID = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUUID, fullName = localFullName, addressUuid = addressUUID, syncStatus = SyncStatus.DONE)
    whenever(mockPatientDao.getOne(patientUUID)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(addressUUID).toPayload()
    val serverPatientWithoutPhone = PatientPayload(
        uuid = patientUUID,
        fullName = remoteFullName,
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = mock(),
        createdAt = mock(),
        updatedAt = mock(),
        address = serverAddress,
        phoneNumbers = null)

    repository.mergeWithLocalData(listOf(serverPatientWithoutPhone)).blockingAwait()

    verify(mockPatientDao).save(argThat<List<Patient>> { first().searchableName == expectedSearchableName })
  }

  @Test
  @Parameters(value = [
    "Name, Name",
    "Name   Surname, NameSurname",
    "Name Middle.Surname, NameMiddleSurname",
    "Name \tSurname, NameSurname"
  ])
  fun `when searching for patients without age bound, strip the search query of any whitespace or punctuation`(
      query: String,
      expectedSearchQuery: String
  ) {
    whenever(mockFuzzyPatientSearchDao.searchForPatientsWithNameLike(any())).thenReturn(Single.just(emptyList()))
    whenever(mockPatientSearchResultDao.search(any())).thenReturn(Flowable.just(emptyList()))
    whenever(database.patientSearchDao()).thenReturn(mockPatientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(mockFuzzyPatientSearchDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)

    repository.search(query).blockingFirst()

    verify(mockPatientSearchResultDao).search(eq(expectedSearchQuery))
  }

  @Test
  fun `when searching for patients with age bound, strip the search query of any whitespace or punctuation`() {
    whenever(mockPatientSearchResultDao.search(any(), any(), any())).thenReturn(Flowable.just(emptyList()))
    whenever(mockFuzzyPatientSearchDao.searchForPatientsWithNameLikeAndAgeWithin(any(), any(), any())).thenReturn(Single.just(emptyList()))
    whenever(database.patientSearchDao()).thenReturn(mockPatientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(mockFuzzyPatientSearchDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)

    repository.search("Name   Surname", 40).blockingFirst()

    verify(mockPatientSearchResultDao).search(eq("NameSurname"), any(), any())
  }

  @Test
  fun `when the fuzzy patient search returns results, they must be at the head of the final results list`() {
    val patientSearchResultTemplate = PatientMocker.patientSearchResult()

    val actualResults = listOf(patientSearchResultTemplate.copy(uuid = UUID.randomUUID()), patientSearchResultTemplate.copy(UUID.randomUUID()))
    val fuzzyResults = listOf(patientSearchResultTemplate.copy(uuid = UUID.randomUUID()))
    whenever(mockPatientSearchResultDao.search(any())).thenReturn(Flowable.just(actualResults))
    whenever(mockFuzzyPatientSearchDao.searchForPatientsWithNameLike(any())).thenReturn(Single.just(fuzzyResults))
    whenever(database.patientSearchDao()).thenReturn(mockPatientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(mockFuzzyPatientSearchDao)

    repository.search("test")
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
    whenever(mockPatientSearchResultDao.search(any())).thenReturn(Flowable.just(actualResults))
    whenever(mockFuzzyPatientSearchDao.searchForPatientsWithNameLike(any())).thenReturn(Single.just(fuzzyResults))
    whenever(database.patientSearchDao()).thenReturn(mockPatientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(mockFuzzyPatientSearchDao)

    repository.search("test")
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
    whenever(database.patientDao()).thenReturn(mockPatientDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(mockFuzzyPatientSearchDao)
    whenever(mockFuzzyPatientSearchDao.updateTableForPatients(any())).thenReturn(Completable.complete())

    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(mockPatientDao.getOne(patientUuid)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(uuid = addressUuid).toPayload()
    val serverPatientWithoutPhone = PatientPayload(
        uuid = patientUuid,
        fullName = "name",
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = mock(),
        createdAt = mock(),
        updatedAt = mock(),
        address = serverAddress,
        phoneNumbers = null)

    repository.mergeWithLocalData(listOf(serverPatientWithoutPhone)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(mockPatientDao).save(argThat<List<Patient>> { isNotEmpty() })
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isNotEmpty() })

    } else {
      verify(mockPatientDao).save(argThat<List<Patient>> { isEmpty() })
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isEmpty() })
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
    whenever(database.patientDao()).thenReturn(mockPatientDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(mockPatientPhoneNumberDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(mockFuzzyPatientSearchDao)
    whenever(mockFuzzyPatientSearchDao.updateTableForPatients(any())).thenReturn(Completable.complete())

    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(mockPatientDao.getOne(patientUuid)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(uuid = addressUuid).toPayload()
    val serverPatientWithPhone = PatientPayload(
        uuid = patientUuid,
        fullName = "name",
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = mock(),
        createdAt = mock(),
        updatedAt = mock(),

        address = serverAddress,
        phoneNumbers = listOf(PatientPhoneNumberPayload(UUID.randomUUID(), "1232", mock(), false, mock(), mock())))

    repository.mergeWithLocalData(listOf(serverPatientWithPhone)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isNotEmpty() })
      verify(mockPatientDao).save(argThat<List<Patient>> { isNotEmpty() })
      verify(mockPatientPhoneNumberDao).save(argThat { isNotEmpty() })

    } else {
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isEmpty() })
      verify(mockPatientDao).save(argThat<List<Patient>> { isEmpty() })
      verify(mockPatientPhoneNumberDao, never()).save(any())
    }
  }
}
