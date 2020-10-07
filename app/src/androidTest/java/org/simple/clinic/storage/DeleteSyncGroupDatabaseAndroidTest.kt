package org.simple.clinic.storage

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus
import java.util.UUID
import javax.inject.Inject

class DeleteSyncGroupDatabaseAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var patientRepository: PatientRepository

  private val facilityDao: Facility.RoomDao by lazy { database.facilityDao() }

  private val appointmentDao: Appointment.RoomDao by lazy { database.appointmentDao() }

  private val groupUuid = UUID.fromString("c30b1c77-3b30-413b-8f42-270ae0a6543d")
  private val currentSyncGroup = "1b247820-c070-40f7-8731-75fe986d0147"
  private val otherSyncGroup = "49aa2533-54e7-4456-b741-e203f3bf7ce1"

  private val currentFacility = TestData.facility(
      uuid = UUID.fromString("b141c2f3-a0b7-4bc0-8475-748a6c7a6e41"),
      name = "PHC Obvious",
      syncGroup = currentSyncGroup,
      groupUuid = groupUuid
  )
  private val otherFacilityInCurrentSyncGroup = TestData.facility(
      uuid = UUID.fromString("bfb99432-f4d7-4522-8229-85d9ec94a979"),
      name = "DH Nilenso",
      syncGroup = currentSyncGroup,
      groupUuid = groupUuid
  )
  private val facilityInAnotherSyncGroup = TestData.facility(
      uuid = UUID.fromString("90582970-9aed-46e4-a16b-45671859701a"),
      name = "CHC RTSL",
      syncGroup = otherSyncGroup,
      groupUuid = groupUuid
  )

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    facilityDao.save(listOf(currentFacility, otherFacilityInCurrentSyncGroup, facilityInAnotherSyncGroup))
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }

  @Test
  fun deleting_the_sync_group_data_should_delete_all_patients_not_registered_or_assigned_in_current_sync_group() {
    // given
    val patientRegisteredInCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("5ee3a570-b561-4b5c-bfd9-d8a680f0a16c"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = currentFacility.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )
    val patientAssignedToCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("771beb4d-3f5b-4cf6-b5f1-8db97d2d2ba2"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = currentFacility.uuid
    )
    val patientRegisteredAtOtherFacilityInCurrentSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("58076d1b-0fd9-430e-8bc4-d452f1af5f97"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )
    val patientAssignedToOtherFacilityInCurrentSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("72ce2033-4b46-4617-9c0d-fd2a54c3ddfa"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = otherFacilityInCurrentSyncGroup.uuid
    )
    val patientInAnotherSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("7f9a581d-8a0c-414b-9b39-c3c3877172fa"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )
    val unsyncedPatientInAnotherSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("b6a540b4-f4b4-45c3-8d3c-70a8a577022e"),
        syncStatus = SyncStatus.PENDING,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )

    val allPatientProfiles = listOf(
        patientRegisteredInCurrentFacility,
        patientAssignedToCurrentFacility,
        patientRegisteredAtOtherFacilityInCurrentSyncGroup,
        patientAssignedToOtherFacilityInCurrentSyncGroup,
        patientInAnotherSyncGroup,
        unsyncedPatientInAnotherSyncGroup
    )
    patientRepository.save(allPatientProfiles).blockingAwait()
    assertThat(patientRepository.allPatientProfiles()).containsExactlyElementsIn(allPatientProfiles)

    // when
    database.deletePatientsNotInFacilitySyncGroup(currentFacility)

    // then
    assertThat(patientRepository.allPatientProfiles()).containsExactly(
        patientRegisteredInCurrentFacility,
        patientAssignedToCurrentFacility,
        patientRegisteredAtOtherFacilityInCurrentSyncGroup,
        patientAssignedToOtherFacilityInCurrentSyncGroup,
        unsyncedPatientInAnotherSyncGroup
    )
  }

  @Test
  fun deleting_the_sync_group_data_should_not_delete_patients_having_a_scheduled_appointment_in_the_current_sync_group() {
    // given
    val patientWithScheduledAppointmentInCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("7f9a581d-8a0c-414b-9b39-c3c3877172fa"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )
    val patientWithScheduledAppointmentInOtherFacilityInCurrentSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("5e539613-28d1-4d72-96aa-2ce24052e643"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )
    val patientWithCancelledAppointment = TestData.patientProfile(
        patientUuid = UUID.fromString("b6a540b4-f4b4-45c3-8d3c-70a8a577022e"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )
    val patientWithVisitedAppointment = TestData.patientProfile(
        patientUuid = UUID.fromString("16276cd1-36e9-42a2-8d2f-cb7d9da6de14"),
        syncStatus = SyncStatus.DONE,
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid
    )

    val allPatientProfiles = listOf(
        patientWithScheduledAppointmentInCurrentFacility,
        patientWithScheduledAppointmentInOtherFacilityInCurrentSyncGroup,
        patientWithCancelledAppointment,
        patientWithVisitedAppointment
    )
    patientRepository.save(allPatientProfiles).blockingAwait()
    assertThat(patientRepository.allPatientProfiles()).containsExactlyElementsIn(allPatientProfiles)

    val scheduledAppointmentInCurrentFacility = TestData.appointment(
        uuid = UUID.fromString("ffe584b3-e613-4056-848a-3c53ffce5986"),
        patientUuid = patientWithScheduledAppointmentInCurrentFacility.patientUuid,
        facilityUuid = currentFacility.uuid,
        status = Appointment.Status.Scheduled,
        cancelReason = null,
        creationFacilityUuid = facilityInAnotherSyncGroup.uuid
    )
    val scheduledAppointmentInOtherFacilityInCurrentSyncGroup = TestData.appointment(
        uuid = UUID.fromString("df48b2ef-af5d-4127-9e8e-0877e4ff0402"),
        patientUuid = patientWithScheduledAppointmentInOtherFacilityInCurrentSyncGroup.patientUuid,
        facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
        status = Appointment.Status.Scheduled,
        cancelReason = null,
        creationFacilityUuid = facilityInAnotherSyncGroup.uuid
    )
    val cancelledAppointment = TestData.appointment(
        uuid = UUID.fromString("4280ab0c-4c9e-4de1-b01c-966a85ec753e"),
        patientUuid = patientWithCancelledAppointment.patientUuid,
        facilityUuid = currentFacility.uuid,
        status = Appointment.Status.Cancelled,
        cancelReason = AppointmentCancelReason.PatientNotResponding,
        creationFacilityUuid = facilityInAnotherSyncGroup.uuid
    )
    val visitedAppointment = TestData.appointment(
        uuid = UUID.fromString("4280ab0c-4c9e-4de1-b01c-966a85ec753e"),
        patientUuid = patientWithCancelledAppointment.patientUuid,
        facilityUuid = currentFacility.uuid,
        status = Appointment.Status.Visited,
        cancelReason = null,
        creationFacilityUuid = facilityInAnotherSyncGroup.uuid
    )

    val allAppointments = listOf(
        scheduledAppointmentInCurrentFacility,
        scheduledAppointmentInOtherFacilityInCurrentSyncGroup,
        cancelledAppointment,
        visitedAppointment
    )
    appointmentDao.save(allAppointments)

    // when
    database.deletePatientsNotInFacilitySyncGroup(currentFacility)

    // then
    assertThat(patientRepository.allPatientProfiles()).containsExactly(
        patientWithScheduledAppointmentInCurrentFacility,
        patientWithScheduledAppointmentInOtherFacilityInCurrentSyncGroup
    )
  }
}
