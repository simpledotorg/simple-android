package org.simple.clinic.patient

import androidx.paging.PositionalDataSource
import androidx.paging.PositionalDataSource.LoadRangeParams
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.searchresultsview.SearchResultsItemType
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class PatientSearchDataSourceTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var config: PatientConfig

  @Before
  fun setup() = TestClinicApp.appComponent().inject(this)

  @Test
  fun `load_range_should_work_correctly`() {
    //given
    val testPersonUuid1 = UUID.fromString("69a94a8f-3f67-4fcd-ba85-6c7ba7b811e4")
    val testPersonUuid2 = UUID.fromString("ee783efa-fa0f-4e1d-98c7-3007910391a2")

    val testAddressUuid1 = UUID.fromString("be003b85-4abe-4be6-a574-95a5b15f0b30")
    val testAddressUuid2 = UUID.fromString("1d921b06-aebb-484c-9aa2-e370169464ea")

    val testFacilityUuid1 = UUID.fromString("a8d64ee3-fa7e-4b37-8143-45e22a834e2b")
    val testFacilityUuid2 = UUID.fromString("6eec8077-b131-470d-9e22-79af6518cb05")

    val facility1 = TestData.facility(uuid = testFacilityUuid1, name = "CHC Bucho")
    val facility2 = TestData.facility(uuid = testFacilityUuid2, name = "Test Hospital")

    val fullName1 = "TestName1"
    val fullName2 = "TestName2"

    val instantTime = Instant.parse("2018-01-01T00:00:00Z")
    
    val testPatient1 = TestData.patient(uuid = testPersonUuid1, fullName = fullName1, addressUuid = testAddressUuid1,
        gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null,
        recordedAt = instantTime,
        syncStatus = SyncStatus.DONE,
        reminderConsent = Granted,
        deletedReason = null,
        registeredFacilityId = testFacilityUuid1,
        assignedFacilityId = testFacilityUuid1)

    val testPatient2 = TestData.patient(uuid = testPersonUuid2, fullName = fullName2, addressUuid = testAddressUuid2, gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null,
        recordedAt = instantTime,
        syncStatus = SyncStatus.DONE,
        reminderConsent = Granted,
        deletedReason = null,
        registeredFacilityId = testFacilityUuid2,
        assignedFacilityId = testFacilityUuid2
    )

    val number1 = "9876798767"
    val number2 = "9877838473"

    val testPhoneNumberUuid1 = UUID.fromString("8b1277a1-2c6f-41ab-b3f9-525b66193059")
    val testPhoneNumber1 = TestData.patientPhoneNumber(testPhoneNumberUuid1, patientUuid = testPersonUuid1, number = number1)
    val testPhoneNumberUuid2 = UUID.fromString("dd8ccb63-4dff-4ac9-990f-8207276d45d6")
    val testPhoneNumber2 = TestData.patientPhoneNumber(uuid = testPhoneNumberUuid2, patientUuid = testPersonUuid2, number = number2)

    val testAddress1 = PatientAddress(
        uuid = testAddressUuid1,
        streetAddress = "test",
        colonyOrVillage = "test",
        district = "test",
        zone = null,
        state = "test",
        country = "test",
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null)

    val testAddress2 = PatientAddress(
        uuid = testAddressUuid2,
        streetAddress = "test",
        colonyOrVillage = "test",
        district = "test",
        zone = null,
        state = "test",
        country = "test",
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null)

    val testPatientSearchResult1 = TestData.patientSearchResult(
        uuid = testPersonUuid1,
        fullName = fullName1,
        phoneNumber = number1,
        gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        address = testAddress1,
        syncStatus = SyncStatus.DONE,
        phoneType = PatientPhoneNumberType.Mobile,
        phoneNumberUuid = testPhoneNumberUuid1,
        phoneActive = true,
        phoneCreatedAt = instantTime,
        phoneUpdatedAt = instantTime,
        lastSeen = PatientSearchResult.LastSeen(
            lastSeenOn = instantTime,
            lastSeenAtFacilityName = "CHC Bucho",
            lastSeenAtFacilityUuid = testFacilityUuid1))

    val testPatientSearchResult2 = TestData.patientSearchResult(uuid = testPersonUuid2, fullName = fullName2, phoneNumber = number2, gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        address = testAddress2,
        syncStatus = SyncStatus.DONE,
        phoneType = PatientPhoneNumberType.Mobile,
        phoneNumberUuid = testPhoneNumberUuid2,
        phoneActive = true,
        phoneCreatedAt = instantTime,
        phoneUpdatedAt = instantTime,
        lastSeen = PatientSearchResult.LastSeen(
            lastSeenOn = instantTime,
            lastSeenAtFacilityName = "Test Hospital",
            lastSeenAtFacilityUuid = testFacilityUuid2))

    val testPatientToFacilityIds1 = TestData.bloodPressureMeasurement(patientUuid = testPersonUuid1, facilityUuid = testFacilityUuid1, recordedAt = instantTime)
    val testPatientToFacilityIds2 = TestData.bloodPressureMeasurement(patientUuid = testPersonUuid2, facilityUuid = testFacilityUuid2, recordedAt = instantTime)

    //when
    val params = LoadRangeParams(0, 4)

    val dataSource = PatientSearchResultDataSource(
        appDatabase,
        { facility1 },
        appDatabase.patientSearchDao().searchByNamePaginated("Test").create() as PositionalDataSource<PatientSearchResult>,
    )

    appDatabase.addressDao().save(listOf(testAddress1, testAddress2))
    appDatabase.patientDao().save(listOf(testPatient1, testPatient2))
    appDatabase.phoneNumberDao().save(listOf(testPhoneNumber1, testPhoneNumber2))
    appDatabase.facilityDao().save(listOf(facility1, facility2))
    appDatabase.bloodPressureDao().save(listOf(testPatientToFacilityIds1, testPatientToFacilityIds2))

    //then
    dataSource.loadRange(params, object : PositionalDataSource.LoadRangeCallback<SearchResultsItemType>() {
      override fun onResult(data: MutableList<SearchResultsItemType>) {
        assertThat(data)
            .containsExactlyElementsIn(
                SearchResultsItemType.from(PatientSearchResults(
                    visitedCurrentFacility = listOf(testPatientSearchResult1),
                    notVisitedCurrentFacility = listOf(testPatientSearchResult2),
                    currentFacility = facility1)))
      }
    })
  }

  @Test
  fun `load_initial_should_work_correctly`() {
    //given
    val testPersonUuid1 = UUID.fromString("69a94a8f-3f67-4fcd-ba85-6c7ba7b811e4")
    val testPersonUuid2 = UUID.fromString("ee783efa-fa0f-4e1d-98c7-3007910391a2")

    val testAddressUuid1 = UUID.fromString("be003b85-4abe-4be6-a574-95a5b15f0b30")
    val testAddressUuid2 = UUID.fromString("1d921b06-aebb-484c-9aa2-e370169464ea")

    val testFacilityUuid1 = UUID.fromString("a8d64ee3-fa7e-4b37-8143-45e22a834e2b")
    val testFacilityUuid2 = UUID.fromString("6eec8077-b131-470d-9e22-79af6518cb05")

    val facility1 = TestData.facility(uuid = testFacilityUuid1, name = "CHC Bucho")
    val facility2 = TestData.facility(uuid = testFacilityUuid2, name = "Test Hospital")

    val fullName1 = "TestName1"
    val fullName2 = "TestName2"

    val instantTime = Instant.parse("2018-01-01T00:00:00Z")

    val testPatient1 = TestData.patient(uuid = testPersonUuid1, fullName = fullName1, addressUuid = testAddressUuid1,
        gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null,
        recordedAt = instantTime,
        syncStatus = SyncStatus.DONE,
        reminderConsent = Granted,
        deletedReason = null,
        registeredFacilityId = testFacilityUuid1,
        assignedFacilityId = testFacilityUuid1)

    val testPatient2 = TestData.patient(uuid = testPersonUuid2, fullName = fullName2, addressUuid = testAddressUuid2, gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null,
        recordedAt = instantTime,
        syncStatus = SyncStatus.DONE,
        reminderConsent = Granted,
        deletedReason = null,
        registeredFacilityId = testFacilityUuid2,
        assignedFacilityId = testFacilityUuid2
    )

    val number1 = "9876798767"
    val number2 = "9877838473"

    val testPhoneNumberUuid1 = UUID.fromString("8b1277a1-2c6f-41ab-b3f9-525b66193059")
    val testPhoneNumber1 = TestData.patientPhoneNumber(testPhoneNumberUuid1, patientUuid = testPersonUuid1, number = number1)
    val testPhoneNumberUuid2 = UUID.fromString("dd8ccb63-4dff-4ac9-990f-8207276d45d6")
    val testPhoneNumber2 = TestData.patientPhoneNumber(uuid = testPhoneNumberUuid2, patientUuid = testPersonUuid2, number = number2)

    val testAddress1 = PatientAddress(
        uuid = testAddressUuid1,
        streetAddress = "test",
        colonyOrVillage = "test",
        district = "test",
        zone = null,
        state = "test",
        country = "test",
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null)

    val testAddress2 = PatientAddress(
        uuid = testAddressUuid2,
        streetAddress = "test",
        colonyOrVillage = "test",
        district = "test",
        zone = null,
        state = "test",
        country = "test",
        createdAt = instantTime,
        updatedAt = instantTime,
        deletedAt = null)

    val testPatientSearchResult1 = TestData.patientSearchResult(
        uuid = testPersonUuid1,
        fullName = fullName1,
        phoneNumber = number1,
        gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        address = testAddress1,
        syncStatus = SyncStatus.DONE,
        phoneType = PatientPhoneNumberType.Mobile,
        phoneNumberUuid = testPhoneNumberUuid1,
        phoneActive = true,
        phoneCreatedAt = instantTime,
        phoneUpdatedAt = instantTime,
        lastSeen = PatientSearchResult.LastSeen(
            lastSeenOn = instantTime,
            lastSeenAtFacilityName = "CHC Bucho",
            lastSeenAtFacilityUuid = testFacilityUuid1))

    val testPatientSearchResult2 = TestData.patientSearchResult(uuid = testPersonUuid2, fullName = fullName2, phoneNumber = number2, gender = Male,
        dateOfBirth = LocalDate.parse("1980-01-01"),
        age = Age(value = 22, updatedAt = instantTime),
        status = PatientStatus.Active,
        createdAt = instantTime,
        updatedAt = instantTime,
        address = testAddress2,
        syncStatus = SyncStatus.DONE,
        phoneType = PatientPhoneNumberType.Mobile,
        phoneNumberUuid = testPhoneNumberUuid2,
        phoneActive = true,
        phoneCreatedAt = instantTime,
        phoneUpdatedAt = instantTime,
        lastSeen = PatientSearchResult.LastSeen(
            lastSeenOn = instantTime,
            lastSeenAtFacilityName = "Test Hospital",
            lastSeenAtFacilityUuid = testFacilityUuid2))

    val testPatientToFacilityIds1 = TestData.bloodPressureMeasurement(patientUuid = testPersonUuid1, facilityUuid = testFacilityUuid1, recordedAt = instantTime)
    val testPatientToFacilityIds2 = TestData.bloodPressureMeasurement(patientUuid = testPersonUuid2, facilityUuid = testFacilityUuid2, recordedAt = instantTime)

    //when
    val params = PositionalDataSource.LoadInitialParams(
        0,
        20,
        20,
        false
    )

    val dataSource = PatientSearchResultDataSource(
        appDatabase,
        { facility1 },
        appDatabase.patientSearchDao().searchByNamePaginated("Test").create() as PositionalDataSource<PatientSearchResult>,
    )

    appDatabase.addressDao().save(listOf(testAddress1, testAddress2))
    appDatabase.patientDao().save(listOf(testPatient1, testPatient2))
    appDatabase.phoneNumberDao().save(listOf(testPhoneNumber1, testPhoneNumber2))
    appDatabase.facilityDao().save(listOf(facility1, facility2))
    appDatabase.bloodPressureDao().save(listOf(testPatientToFacilityIds1, testPatientToFacilityIds2))

    //then
    dataSource.loadInitial(params, object : PositionalDataSource.LoadInitialCallback<SearchResultsItemType>() {
      override fun onResult(data: MutableList<SearchResultsItemType>, position: Int, totalCount: Int) {
        assertThat(data)
            .containsExactlyElementsIn(
                SearchResultsItemType.from(PatientSearchResults(
                    visitedCurrentFacility = listOf(testPatientSearchResult1),
                    notVisitedCurrentFacility = listOf(testPatientSearchResult2),
                    currentFacility = facility1)))
      }

      override fun onResult(data: MutableList<SearchResultsItemType>, position: Int) {
      }
    })
  }
}
