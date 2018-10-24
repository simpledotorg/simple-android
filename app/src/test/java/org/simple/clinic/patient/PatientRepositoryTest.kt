package org.simple.clinic.patient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
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

  private val database = mock<AppDatabase>()
  private val clock = TestClock()
  private val patientSearchResultDao = mock<PatientSearchResult.RoomDao>()
  private val patientDao = mock<Patient.RoomDao>()
  private val patientAddressDao = mock<PatientAddress.RoomDao>()
  private val patientPhoneNumberDao = mock<PatientPhoneNumber.RoomDao>()
  private val fuzzyPatientSearchDao = mock<PatientFuzzySearch.PatientFuzzySearchDao>()
  private val dobValidator = mock<DateOfBirthFormatValidator>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val numberValidator = mock<PhoneNumberValidator>()

  @Before
  fun setUp() {
    ageFuzzer = mock()
    whenever(ageFuzzer.bounded(any())).thenReturn(BoundedAge(LocalDate.now(clock), LocalDate.now(clock)))
    repository = PatientRepository(database, dobValidator, facilityRepository, userSession, numberValidator, clock, ageFuzzer)

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
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLike(any())).thenReturn(Single.just(emptyList()))
    whenever(patientSearchResultDao.search(any(), any())).thenReturn(Flowable.just(emptyList()))
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)
    whenever(database.addressDao()).thenReturn(patientAddressDao)

    repository.search(query).blockingFirst()

    verify(patientSearchResultDao).search(eq(expectedSearchQuery), any())
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
    whenever(patientSearchResultDao.search(any(), any())).thenReturn(Flowable.just(actualResults))
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLike(any())).thenReturn(Single.just(fuzzyResults))
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)

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
    whenever(patientSearchResultDao.search(any(), any())).thenReturn(Flowable.just(actualResults))
    whenever(fuzzyPatientSearchDao.searchForPatientsWithNameLike(any())).thenReturn(Single.just(fuzzyResults))
    whenever(database.patientSearchDao()).thenReturn(patientSearchResultDao)
    whenever(database.fuzzyPatientSearchDao()).thenReturn(fuzzyPatientSearchDao)

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
}
