package org.simple.clinic.patient.onlinelookup.api

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApi
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.rules.ServerRegistrationAtFacilityRule
import org.simple.clinic.util.Rules
import org.simple.clinic.util.TestUtcClock
import java.time.LocalDate
import javax.inject.Inject

class LookupPatientOnlineApiIntegrationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerRegistrationAtFacilityRule(::pickFacilityWithTwoDifferentSyncGroups))

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var patientSyncApi: PatientSyncApi

  @Inject
  lateinit var medicalHistorySyncApi: MedicalHistorySyncApi

  @Inject
  lateinit var lookupPatientOnline: LookupPatientOnline

  private val currentFacility by lazy { facilityRepository.currentFacilityImmediate()!! }

  private val facilitiesInOtherSyncGroup by lazy {
    val currentFacilitySyncGroup = currentFacility.syncGroup

    facilityRepository
        .facilitiesInCurrentGroup()
        .blockingFirst()
        .partition { facility -> facility.syncGroup == currentFacilitySyncGroup }
        .second
  }

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.parse("2021-01-01"))
  }

  /*
  * The online lookup API was designed for a specific use-case: One where a patient assigned to a
  * facility in a _different_ sync group than the current one visits this facility as a one-time
  * event and we need to look this patient up online, otherwise we risk creating duplicate records,
  * which are cumbersome to merge.
  *
  * In order to verify that the API works as expected, we have to ensure that we record a patient
  * in a facility from a different sync group than the current one and then verify that the record
  * can be looked up from the current facility.
  *
  * However, this also means that when picking the current facility to register the user at, we
  * need to pick a facility in a group that has *at least* two different sync groups. This method
  * picks a facility to register based on that criteria.
  **/
  private fun pickFacilityWithTwoDifferentSyncGroups(facilities: List<Facility>): Facility {
    val syncGroupSizesByFacilityGroup = facilities
        .groupBy(
            keySelector = { it.groupUuid },
            valueTransform = { it.syncGroup }
        )
        .mapValues { (_, syncGroupIds) -> syncGroupIds.distinct().count() }

    val facilityGroupsWithAtLeastTwoSyncGroups = syncGroupSizesByFacilityGroup
        .filter { (_, syncGroupsInFacilityGroup) -> syncGroupsInFacilityGroup > 1 }
        .map { (facilityGroupId, _) -> facilityGroupId!! }

    assertThat(facilityGroupsWithAtLeastTwoSyncGroups).isNotEmpty()

    val registrationFacilityGroup = facilityGroupsWithAtLeastTwoSyncGroups.random()

    return facilities
        .filter { it.groupUuid == registrationFacilityGroup }
        .random()
  }
}
