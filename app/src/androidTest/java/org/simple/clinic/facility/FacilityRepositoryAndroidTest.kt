package org.simple.clinic.facility

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.reactivex.rxkotlin.toCompletable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.PatientFaker
import org.simple.clinic.TestClinicApp
import org.simple.clinic.user.LoggedInUser
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class FacilityRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var repository: FacilityRepository

  @Inject
  lateinit var patientFaker: PatientFaker

  private lateinit var user: LoggedInUser

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    database.clearAllTables()

    user = patientFaker.loggedInUser()
    database.userDao().createOrUpdate(user)
  }

  @Test
  fun when_associating_a_user_with_facilities_then_only_one_facility_should_be_set_as_current_facility() {
    val facility1 = patientFaker.facility()
    val facility2 = patientFaker.facility()
    val facility3 = patientFaker.facility()
    val facility4 = patientFaker.facility()
    val facilities = listOf(facility1, facility2, facility3, facility4)
    database.facilityDao().save(facilities)

    val facilityIds = facilities.map { it.uuid }
    repository.associateUserWithFacilities(user, facilityIds, currentFacility = facility2.uuid).blockingAwait()

    // 1. Check facilities for this user.
    val facilityUuidsForUser = repository.facilityUuidsForUser(user).blockingFirst()
    assertThat(facilityUuidsForUser).hasSize(facilityIds.size)
    assertThat(facilityUuidsForUser).containsAllIn(facilityIds)

    // 2. Check current facility for this user.
    val currentFacility = repository.currentFacility(user).blockingFirst()
    assertThat(currentFacility).isEqualTo(facility2)
  }

  @Test
  fun when_changing_the_current_facility_for_a_user_then_the_current_facility_should_get_set() {
    val facility1 = patientFaker.facility()
    val facility2 = patientFaker.facility()
    val facilities = listOf(facility1, facility2)
    database.facilityDao().save(facilities)

    val facilityIds = facilities.map { it.uuid }

    repository.associateUserWithFacilities(user, facilityIds, currentFacility = facility1.uuid)
        .andThen({
          database.userFacilityMappingDao().changeCurrentFacility(user.uuid, facility2.uuid)
        }.toCompletable())
        .blockingAwait()

    val mappings = database.userFacilityMappingDao().mappingsForUser(user.uuid).blockingFirst()

    val facility1Mapping = mappings.first { it.facilityUuid == facility2.uuid }
    assertThat(facility1Mapping.isCurrentFacility).isTrue()
  }

  @Test
  fun when_changing_the_facility_for_a_user_the_older_current_facility_should_get_unset() {
    val facility1 = patientFaker.facility()
    val facility2 = patientFaker.facility()
    val facilities = listOf(facility1, facility2)
    database.facilityDao().save(facilities)

    val facilityIds = facilities.map { it.uuid }

    repository.associateUserWithFacilities(user, facilityIds, currentFacility = facility1.uuid)
        .andThen({
          database.userFacilityMappingDao().changeCurrentFacility(user.uuid, facility2.uuid)
        }.toCompletable())
        .blockingAwait()

    val mappings = database.userFacilityMappingDao().mappingsForUser(user.uuid).blockingFirst()

    val facility1Mapping = mappings.first { it.facilityUuid == facility1.uuid }
    assertThat(facility1Mapping.isCurrentFacility).isFalse()
  }

  @Test(expected = AssertionError::class)
  fun foo() {
    val facility1 = patientFaker.facility()
    val facility2 = patientFaker.facility()
    val facility3 = patientFaker.facility()
    val facilityIds = listOf(facility1, facility2).map { it.uuid }

    database.userFacilityMappingDao().insertOrUpdate(user, facilityIds, facility3.uuid)
  }
}
