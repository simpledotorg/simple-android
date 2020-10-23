package org.simple.clinic.facility

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject


class FacilityRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var repository: FacilityRepository

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilityDao: Facility.RoomDao

  @Inject
  lateinit var userDao: User.RoomDao

  @get:Rule
  val rule: RuleChain = Rules.global()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    database.clearAllTables()
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }

  @Test
  fun facilities_should_be_ordered_alphabetically() {
    val facilityB = testData.facility(uuid = UUID.randomUUID(), name = "Facility B")
    val facilityD = testData.facility(uuid = UUID.randomUUID(), name = "Phacility D")
    val facilityA = testData.facility(uuid = UUID.randomUUID(), name = "Facility A")
    val facilityC = testData.facility(uuid = UUID.randomUUID(), name = "Phacility C")

    val facilitiesToStore = listOf(facilityB, facilityD, facilityA, facilityC)
    facilityDao.save(facilitiesToStore)

    val allStoredFacilities = repository.facilities().blockingFirst()
    assertThat(allStoredFacilities).isEqualTo(listOf(facilityA, facilityB, facilityC, facilityD))

    val allFilteredFacilities = repository.facilities(searchQuery = "Pha").blockingFirst()
    assertThat(allFilteredFacilities).isEqualTo(listOf(facilityC, facilityD))
  }

  @Test
  fun when_associating_a_user_with_facilities_then_only_one_facility_should_be_set_as_current_facility() {
    val facility1 = testData.facility(uuid = UUID.randomUUID(), name = "facility1")
    val facility2 = testData.facility(uuid = UUID.randomUUID(), name = "facility2")
    val facility3 = testData.facility(uuid = UUID.randomUUID(), name = "facility3")
    val facility4 = testData.facility(uuid = UUID.randomUUID(), name = "facility4")

    val facilities = listOf(facility1, facility2, facility3, facility4)
    facilityDao.save(facilities)

    val user = TestData.loggedInUser(
        uuid = UUID.fromString("e8c307e4-63e4-43c6-ba74-a51fb494ada8"),
        registrationFacilityUuid = facility1.uuid
    )
    userDao.createOrUpdate(user)

    val facilityIds = facilities.map { it.uuid }
    repository.setCurrentFacilityImmediate(facility3)
    repository.setCurrentFacilityImmediate(facility4)

    val currentFacility = repository.currentFacility().blockingFirst()
    assertThat(currentFacility).isEqualTo(facility4)
    // Regression to verify that the user's registration facility does not get overriden when changing current facility
    assertThat(userDao.userImmediate()!!.registrationFacilityUuid).isEqualTo(facility1.uuid)
  }

  @Test
  fun when_changing_the_current_facility_for_a_user_then_the_current_facility_should_get_set() {
    val facility1 = testData.facility(uuid = UUID.fromString("0515190c-9eab-4276-a9d7-3d5fcc2ad4c8"))
    val facility2 = testData.facility(uuid = UUID.fromString("642e1c66-0bf9-4b50-ac5f-029c5a49fd62"))
    val facility3 = testData.facility(uuid = UUID.fromString("75e086d1-77a9-427c-aec7-891739d2c440"))
    val facilities = listOf(facility1, facility2, facility3)
    facilityDao.save(facilities)

    val user = TestData.loggedInUser(
        uuid = UUID.fromString("e8c307e4-63e4-43c6-ba74-a51fb494ada8"),
        registrationFacilityUuid = facility1.uuid
    )
    userDao.createOrUpdate(user)

    repository.setCurrentFacilityImmediate(facility2)
    repository.setCurrentFacilityImmediate(facility3)

    assertThat(repository.currentFacilityImmediate()).isEqualTo(facility3)
  }

  @Test
  fun facilities_should_be_overridable() {
    val facility = testData.facility(uuid = UUID.randomUUID(), name = "Faceeleety")
    facilityDao.save(listOf(facility))

    val correctedFacility = facility.copy(name = "Facility")
    facilityDao.save(listOf(correctedFacility))

    val storedFacility = facilityDao.getOne(correctedFacility.uuid)!!
    assertThat(storedFacility.name).isEqualTo(correctedFacility.name)
  }

  @Test
  fun when_search_query_is_blank_then_all_facilities_should_be_fetched() {
    val facility1 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 1")
    val facility2 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 2")
    val facilities = listOf(facility1, facility2)
    facilityDao.save(facilities)

    val filteredFacilities = repository.facilities(searchQuery = "").blockingFirst()
    assertThat(filteredFacilities).isEqualTo(facilities)
  }

  @Test
  fun when_filtering_by_current_group_and_search_query_is_blank_then_all_facilities_should_be_fetched() {
    val facilityGroup1 = UUID.randomUUID()
    val facilityGroup2 = UUID.randomUUID()

    val facility1 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 1", groupUuid = facilityGroup1)
    val facility2 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 2", groupUuid = facilityGroup1)
    val facility3 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 3", groupUuid = facilityGroup2)

    val facilitiesInGroup1 = listOf(facility1, facility2)
    val facilitiesInGroup2 = listOf(facility3)
    facilityDao.save(facilitiesInGroup1 + facilitiesInGroup2)

    val user = TestData.loggedInUser(
        uuid = UUID.fromString("e8c307e4-63e4-43c6-ba74-a51fb494ada8"),
        registrationFacilityUuid = facility1.uuid
    )
    userDao.createOrUpdate(user)

    associateCurrentFacilityToUser(facilitiesInGroup1.first())

    val filteredFacilities = repository.facilitiesInCurrentGroup(searchQuery = "").blockingFirst()
    assertThat(filteredFacilities).isEqualTo(facilitiesInGroup1)
  }

  private fun associateCurrentFacilityToUser(facility: Facility) {
    repository.setCurrentFacilityImmediate(facility)
  }

  @Test
  fun when_search_query_is_not_blank_then_filtered_facilities_should_be_fetched() {
    val facility1 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 1")
    val facility2 = testData.facility(uuid = UUID.randomUUID(), name = "Phacility 2")
    val facility3 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 3")
    val facility4 = testData.facility(uuid = UUID.randomUUID(), name = "Phacility 4")
    facilityDao.save(listOf(facility1, facility2, facility3, facility4))

    val filteredFacilities = repository.facilities(searchQuery = "hac").blockingFirst()
    assertThat(filteredFacilities).isEqualTo(listOf(facility2, facility4))
  }

  @Test
  fun when_filtering_by_current_group_and_search_query_is_not_blank_then_filtered_facilities_should_be_fetched() {
    val facilityGroup1 = UUID.randomUUID()
    val facilityGroup2 = UUID.randomUUID()

    val facility1 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 1", groupUuid = facilityGroup1)
    val facility2 = testData.facility(uuid = UUID.randomUUID(), name = "Phacility 2", groupUuid = facilityGroup2)
    val facility3 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 3", groupUuid = facilityGroup1)
    val facility4 = testData.facility(uuid = UUID.randomUUID(), name = "Phacility 4", groupUuid = facilityGroup1)

    val facilitiesInGroup1 = listOf(facility1, facility3, facility4)
    val facilitiesInGroup2 = listOf(facility2)
    facilityDao.save(facilitiesInGroup1 + facilitiesInGroup2)

    val user = TestData.loggedInUser(
        uuid = UUID.fromString("e8c307e4-63e4-43c6-ba74-a51fb494ada8"),
        registrationFacilityUuid = facility1.uuid
    )
    userDao.createOrUpdate(user)

    associateCurrentFacilityToUser(facilitiesInGroup2.first())

    val filteredFacilities = repository.facilitiesInCurrentGroup(searchQuery = "hac").blockingFirst()
    assertThat(filteredFacilities).isEqualTo(listOf(facility2))
  }

  @Test
  fun filtering_of_facilities_should_be_case_insensitive() {
    val facility1 = testData.facility(uuid = UUID.randomUUID(), name = "Facility 1")
    val facility2 = testData.facility(uuid = UUID.randomUUID(), name = "Phacility 2")
    val facilities = listOf(facility1, facility2)
    facilityDao.save(facilities)

    val filteredFacilities = repository.facilities(searchQuery = "fac").blockingFirst()
    assertThat(filteredFacilities).isEqualTo(listOf(facility1))
  }

  @Test
  fun filtering_of_facilities_in_current_group_should_be_case_insensitive() {
    val facilityGroup1 = UUID.randomUUID()
    val facilityGroup2 = UUID.randomUUID()

    val group1Facility = testData.facility(uuid = UUID.randomUUID(), name = "Facility 1", groupUuid = facilityGroup1)
    val group2Facility = testData.facility(uuid = UUID.randomUUID(), name = "Phacility 2", groupUuid = facilityGroup2)
    val facilities = listOf(group1Facility, group2Facility)
    facilityDao.save(facilities)

    val user = TestData.loggedInUser(
        uuid = UUID.fromString("e8c307e4-63e4-43c6-ba74-a51fb494ada8"),
        registrationFacilityUuid = group1Facility.uuid
    )
    userDao.createOrUpdate(user)

    associateCurrentFacilityToUser(group1Facility)

    val filteredFacilities = repository.facilitiesInCurrentGroup(searchQuery = "fac").blockingFirst()
    assertThat(filteredFacilities).isEqualTo(listOf(group1Facility))
  }

  @Test
  fun getting_current_facility_immediately_for_the_given_user() {
    val facility1 = testData.facility(
        uuid = UUID.fromString("19822126-a96d-4619-b7be-4477f1f5e429"),
        name = "Facility 1"
    )
    val facility2 = testData.facility(
        uuid = UUID.fromString("978bd4f6-3f13-4ef7-847a-ad315dcd46fa"),
        name = "Facility 2"
    )

    repository.save(listOf(facility1, facility2)).blockingAwait()

    val user = TestData.loggedInUser(
        uuid = UUID.fromString("e8c307e4-63e4-43c6-ba74-a51fb494ada8"),
        registrationFacilityUuid = facility1.uuid
    )
    userDao.createOrUpdate(user)

    associateCurrentFacilityToUser(facility1)

    val currentFacility = repository.currentFacilityImmediate()
    assertThat(currentFacility).isEqualTo(facility1)
  }
}
