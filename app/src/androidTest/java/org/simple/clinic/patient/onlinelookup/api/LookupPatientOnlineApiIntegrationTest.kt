package org.simple.clinic.patient.onlinelookup.api

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.sharedTestCode.TestData
import org.simple.clinic.bloodsugar.sync.BloodSugarPushRequest
import org.simple.clinic.bloodsugar.sync.BloodSugarSyncApi
import org.simple.clinic.bp.sync.BloodPressurePushRequest
import org.simple.clinic.bp.sync.BloodPressureSyncApi
import org.simple.clinic.drugs.sync.PrescriptionPushRequest
import org.simple.clinic.drugs.sync.PrescriptionSyncApi
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPushRequest
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApi
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentPushRequest
import org.simple.clinic.overdue.AppointmentSyncApi
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.sync.PatientPushRequest
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.rules.ServerRegistrationAtFacilityRule
import org.simple.clinic.user.UserSession
import org.simple.sharedTestCode.util.Rules
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class LookupPatientOnlineApiIntegrationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerRegistrationAtFacilityRule(::pickFacilityWithTwoDifferentSyncGroups))

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var patientSyncApi: PatientSyncApi

  @Inject
  lateinit var medicalHistorySyncApi: MedicalHistorySyncApi

  @Inject
  lateinit var bloodPressureSyncApi: BloodPressureSyncApi

  @Inject
  lateinit var bloodSugarSyncApi: BloodSugarSyncApi

  @Inject
  lateinit var appointmentSyncApi: AppointmentSyncApi

  @Inject
  lateinit var prescribedDrugSyncApi: PrescriptionSyncApi

  @Inject
  lateinit var lookupPatientOnline: LookupPatientOnline

  private val currentUser by lazy { userSession.loggedInUserImmediate()!! }

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

  @Test
  fun patient_registered_in_other_sync_group_should_be_fetched_in_online_lookup() {
    // given
    val patientId = UUID.randomUUID()
    val identifier = UUID.randomUUID().toString()

    val facilityFromOtherSyncGroup = facilitiesInOtherSyncGroup.random()

    registerPatientAtFacility(
        identifier = identifier,
        patientId = patientId,
        facility = facilityFromOtherSyncGroup,
        numberOfBloodPressures = 2,
        numberOfBloodSugars = 3,
        numberOfPrescribedDrugs = 4
    )

    // when
    val result = lookupPatientOnline.lookupWithIdentifier(identifier) as LookupPatientOnline.Result.Found

    // then
    assertThat(result.medicalRecords).hasSize(1)

    val medicalRecord = result.medicalRecords.first()
    assertThat(medicalRecord.patient.patientUuid).isEqualTo(patientId)
    assertThat(medicalRecord.patient.businessIds.first().identifier.value).isEqualTo(identifier)
    assertThat(medicalRecord.patient.patient.retainUntil).isNotNull()
    assertThat(medicalRecord.medicalHistory).isNotNull()
    assertThat(medicalRecord.appointments).hasSize(1)
    assertThat(medicalRecord.bloodPressures).hasSize(2)
    assertThat(medicalRecord.bloodSugars).hasSize(3)
    assertThat(medicalRecord.prescribedDrugs).hasSize(4)
  }

  @Suppress("SameParameterValue")
  private fun registerPatientAtFacility(
      identifier: String,
      patientId: UUID,
      facility: Facility,
      numberOfBloodPressures: Int,
      numberOfBloodSugars: Int,
      numberOfPrescribedDrugs: Int
  ) {
    val instant = Instant.now(clock)

    val patientAddressPayload = TestData.addressPayload(
        uuid = UUID.randomUUID(),
        createdAt = instant,
        updatedAt = instant
    )
    val phoneNumbers = TestData.phoneNumberPayload(
        uuid = UUID.randomUUID(),
        type = PatientPhoneNumberType.Mobile,
        createdAt = instant,
        updatedAt = instant
    )
    val businessId = TestData.businessIdPayload(
        uuid = UUID.randomUUID(),
        identifier = identifier,
        identifierType = Identifier.IdentifierType.BpPassport,
        createdAt = instant,
        updatedAt = instant
    )
    val patientPayload = TestData.patientPayload(
        uuid = patientId,
        fullName = "Anish Acharya",
        gender = Gender.Male,
        dateOfBirth = LocalDate.parse("1942-04-01"),
        age = null,
        ageUpdatedAt = null,
        status = PatientStatus.Active,
        createdAt = instant,
        updatedAt = instant,
        recordedAt = instant,
        deletedReason = null,
        registeredFacilityId = facility.uuid,
        assignedFacilityId = facility.uuid,
        address = patientAddressPayload,
        phoneNumbers = listOf(phoneNumbers),
        businessIds = listOf(businessId)
    )
    val patientSyncResponse = patientSyncApi.push(PatientPushRequest(listOf(patientPayload))).execute()
    assertThat(patientSyncResponse.isSuccessful).isTrue()

    val medicalHistoryPayload = TestData.medicalHistoryPayload(
        uuid = UUID.randomUUID(),
        patientUuid = patientId,
        createdAt = instant,
        updatedAt = instant
    )
    val medicalHistorySyncResponse = medicalHistorySyncApi.push(MedicalHistoryPushRequest(listOf(medicalHistoryPayload))).execute()
    assertThat(medicalHistorySyncResponse.isSuccessful).isTrue()

    val appointmentPayloads = listOf(
        TestData.appointmentPayload(
            uuid = UUID.randomUUID(),
            patientUuid = patientId,
            date = LocalDate.now(clock).plusDays(7),
            status = Appointment.Status.Scheduled,
            facilityUuid = facility.uuid,
            creationFacilityUuid = facility.uuid,
            cancelReason = null,
            createdAt = instant,
            updatedAt = instant
        )
    )
    val appointmentSyncResponse = appointmentSyncApi.push(AppointmentPushRequest(appointmentPayloads)).execute()
    assertThat(appointmentSyncResponse.isSuccessful).isTrue()

    val bloodPressurePayloads = (1..numberOfBloodPressures).map {
      TestData.bpPayload(
          uuid = UUID.randomUUID(),
          patientUuid = patientId,
          userUuid = currentUser.uuid,
          facilityUuid = facility.uuid,
          createdAt = instant,
          updatedAt = instant,
          recordedAt = instant
      )
    }
    val bloodPressureSyncResponse = bloodPressureSyncApi.push(BloodPressurePushRequest(bloodPressurePayloads)).execute()
    assertThat(bloodPressureSyncResponse.isSuccessful).isTrue()

    val bloodSugarPayloads = (1..numberOfBloodSugars).map {
      TestData.bloodSugarPayload(
          uuid = UUID.randomUUID(),
          patientUuid = patientId,
          userUuid = currentUser.uuid,
          facilityUuid = facility.uuid,
          createdAt = instant,
          updatedAt = instant,
          recordedAt = instant
      )
    }
    val bloodSugarSyncResponse = bloodSugarSyncApi.push(BloodSugarPushRequest(bloodSugarPayloads)).execute()
    assertThat(bloodSugarSyncResponse.isSuccessful).isTrue()

    val prescribedDrugPayloads = (1..numberOfPrescribedDrugs).map {
      TestData.prescriptionPayload(
          uuid = UUID.randomUUID(),
          patientUuid = patientId,
          facilityUuid = facility.uuid,
          createdAt = instant,
          updatedAt = instant
      )
    }
    val prescribedDrugSyncResponse = prescribedDrugSyncApi.push(PrescriptionPushRequest(prescribedDrugPayloads)).execute()
    assertThat(prescribedDrugSyncResponse.isSuccessful).isTrue()
  }
}
