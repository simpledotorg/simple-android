package org.simple.clinic.facility

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.user.LoggedInUserFacilityMapping
import org.simple.clinic.user.User
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class FacilityRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var repository: FacilityRepository

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var dao: LoggedInUserFacilityMapping.RoomDao

  private lateinit var user: User

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    database.clearAllTables()

    user = testData.loggedInUser()
    database.userDao().createOrUpdate(user)
  }

  @Test
  fun facilities_should_be_ordered_alphabetically() {
    val facilityB = testData.facility(uuid = UUID.randomUUID(), name = "B")
    val facilityD = testData.facility(uuid = UUID.randomUUID(), name = "D")
    val facilityA = testData.facility(uuid = UUID.randomUUID(), name = "A")
    val facilityC = testData.facility(uuid = UUID.randomUUID(), name = "C")

    val facilitiesToStore = listOf(facilityB, facilityD, facilityA, facilityC)
    database.facilityDao().save(facilitiesToStore)

    val returnedFacilities = repository.facilities().blockingFirst()
    val expectedOrdering = listOf(facilityA, facilityB, facilityC, facilityD)
    assertThat(returnedFacilities).isEqualTo(expectedOrdering)
  }

  @Test
  fun when_associating_a_user_with_facilities_then_only_one_facility_should_be_set_as_current_facility() {
    val facility1 = testData.facility(uuid = UUID.randomUUID(), name = "facility1")
    val facility2 = testData.facility(uuid = UUID.randomUUID(), name = "facility2")
    val facility3 = testData.facility(uuid = UUID.randomUUID(), name = "facility3")
    val facility4 = testData.facility(uuid = UUID.randomUUID(), name = "facility4")

    val facilities = listOf(facility1, facility2, facility3, facility4)
    database.facilityDao().save(facilities)

    val facilityIds = facilities.map { it.uuid }
    repository.associateUserWithFacilities(user, facilityIds).blockingAwait()
    repository.setCurrentFacility(user, facility3).blockingAwait()
    repository.setCurrentFacility(user, facility4).blockingAwait()

    // 1. Check facilities for this user.
    val facilityUuidsForUser = repository.facilityUuidsForUser(user).blockingFirst()
    assertThat(facilityUuidsForUser).hasSize(facilities.size)
    assertThat(facilityUuidsForUser).containsAllIn(facilities.map { it.uuid })

    // 2. Check current facility for this user.
    val currentFacility = repository.currentFacility(user).blockingFirst()
    assertThat(currentFacility).isEqualTo(facility4)
  }

  @Test
  fun when_changing_the_current_facility_for_a_user_then_the_current_facility_should_get_set() {
    val facility1 = testData.facility(uuid = UUID.randomUUID())
    val facility2 = testData.facility(uuid = UUID.randomUUID())
    val facility3 = testData.facility(uuid = UUID.randomUUID())
    val facilities = listOf(facility1, facility2, facility3)
    database.facilityDao().save(facilities)

    repository.associateUserWithFacilities(user, facilities.map { it.uuid })
        .andThen(repository.setCurrentFacility(user, facility2))
        .andThen(repository.setCurrentFacility(user, facility3))
        .blockingAwait()

    val mappings = dao.mappingsForUser(user.uuid).blockingFirst()

    val facility3Mapping = mappings.first { it.facilityUuid == facility3.uuid }
    assertThat(facility3Mapping.isCurrentFacility).isTrue()
  }

  @Test
  fun when_changing_the_facility_for_a_user_the_previous_current_facility_should_get_unset() {
    val facility1 = testData.facility(uuid = UUID.randomUUID())
    val facility2 = testData.facility(uuid = UUID.randomUUID())
    val facility3 = testData.facility(uuid = UUID.randomUUID())
    val facilities = listOf(facility1, facility2, facility3)
    database.facilityDao().save(facilities)

    repository.associateUserWithFacilities(user, facilities.map { it.uuid })
        .andThen(repository.setCurrentFacility(user, facility2))
        .andThen(repository.setCurrentFacility(user, facility3))
        .blockingAwait()

    val mappings = dao.mappingsForUser(user.uuid).blockingFirst()

    val facility1Mapping = mappings.first { it.facilityUuid == facility1.uuid }

    assertThat(facility1Mapping.isCurrentFacility).isFalse()
    val facility2Mapping = mappings.first { it.facilityUuid == facility2.uuid }
    assertThat(facility2Mapping.isCurrentFacility).isFalse()
  }

  @Test(expected = AssertionError::class)
  fun when_changing_the_facility_for_a_user_to_a_facility_whose_mapping_does_not_exist_then_an_error_should_be_thrown() {
    val facility1 = testData.facility(uuid = UUID.randomUUID())
    val facility2 = testData.facility(uuid = UUID.randomUUID())
    val facilities = listOf(facility1, facility2)
    database.facilityDao().save(facilities)

    repository.associateUserWithFacility(user, facility1).blockingAwait()
    dao.changeCurrentFacility(user.uuid, newCurrentFacilityUuid = facility2.uuid)
  }
}
