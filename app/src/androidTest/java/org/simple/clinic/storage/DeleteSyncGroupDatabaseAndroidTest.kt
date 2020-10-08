package org.simple.clinic.storage

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
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

  private val facilityDao by lazy { database.facilityDao() }
  private val appointmentDao by lazy { database.appointmentDao() }
  private val bloodPressureMeasurementDao by lazy { database.bloodPressureDao() }
  private val bloodSugarMeasurementDao by lazy { database.bloodSugarDao() }
  private val prescribedDrugDao by lazy { database.prescriptionDao() }
  private val medicalHistoryDao by lazy { database.medicalHistoryDao() }

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

  @Test
  fun deleting_the_sync_group_data_should_delete_blood_pressure_measurements_which_do_not_have_a_linked_patient() {
    // given
    val patientInCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("d1523ba6-bad3-42f2-a920-a503f1a503e3"),
        patientRegisteredFacilityId = currentFacility.uuid,
        patientAssignedFacilityId = currentFacility.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherFacilityInSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("cc131584-b88b-42b8-8f4c-29c93021765f"),
        patientRegisteredFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        patientAssignedFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("5cbe9277-d18a-49ad-a73b-1840a7aba0a9"),
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    patientRepository.save(listOf(
        patientInCurrentFacility,
        patientInOtherFacilityInSyncGroup,
        patientInOtherSyncGroup
    )).blockingAwait()

    val bloodPressuresForPatientInCurrentFacility = listOf(
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("a8ad7e61-19d3-4bb0-97bc-3aff2c5b3165"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("6536ca4a-c053-4d12-8fc6-f05dd210c0d2"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("222ebe94-dfd3-4632-95a4-4ecde30a8ee9"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val bloodPressuresForPatientInOtherFacilityInCurrentSyncGroup = listOf(
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("79c98115-4894-4fb7-8264-fb442a48b225"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("092de4a7-6493-4313-bfab-6bd2741ec143"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("cae126b9-515d-4056-8262-6d37b4a251e1"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val unsyncedBloodPressureMeasurementForPatientInOtherSyncGroup = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("ba9b72e9-da63-4d69-a023-0339a274c34e"),
        patientUuid = patientInOtherSyncGroup.patientUuid,
        facilityUuid = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.PENDING
    )
    val bloodPressuresForPatientInOtherSyncGroup = listOf(
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("75de9139-a7e0-4c55-9cb1-058b76e06da7"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("a3ac1607-fcee-4d8c-b2ea-f6c77b301a33"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodPressureMeasurement(
            uuid = UUID.fromString("2b625418-16b1-46aa-97af-76f9245d9ece"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        unsyncedBloodPressureMeasurementForPatientInOtherSyncGroup
    )

    val allBloodPressureMeasurements = bloodPressuresForPatientInCurrentFacility +
        bloodPressuresForPatientInOtherFacilityInCurrentSyncGroup +
        bloodPressuresForPatientInOtherSyncGroup

    bloodPressureMeasurementDao.save(allBloodPressureMeasurements)
    assertThat(bloodPressureMeasurementDao.getAllBloodPressureMeasurements()).containsExactlyElementsIn(allBloodPressureMeasurements)

    // when
    database.deletePatientsNotInFacilitySyncGroup(currentFacility)

    // then
    val expectedBloodPressureMeasurements = bloodPressuresForPatientInCurrentFacility +
        bloodPressuresForPatientInOtherFacilityInCurrentSyncGroup +
        unsyncedBloodPressureMeasurementForPatientInOtherSyncGroup
    assertThat(bloodPressureMeasurementDao.getAllBloodPressureMeasurements()).containsExactlyElementsIn(expectedBloodPressureMeasurements)
  }

  @Test
  fun deleting_the_sync_group_data_should_delete_blood_sugar_measurements_which_do_not_have_a_linked_patient() {
    // given
    val patientInCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("d1523ba6-bad3-42f2-a920-a503f1a503e3"),
        patientRegisteredFacilityId = currentFacility.uuid,
        patientAssignedFacilityId = currentFacility.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherFacilityInSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("cc131584-b88b-42b8-8f4c-29c93021765f"),
        patientRegisteredFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        patientAssignedFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("5cbe9277-d18a-49ad-a73b-1840a7aba0a9"),
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    patientRepository.save(listOf(
        patientInCurrentFacility,
        patientInOtherFacilityInSyncGroup,
        patientInOtherSyncGroup
    )).blockingAwait()

    val bloodSugarsForPatientInCurrentFacility = listOf(
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("a8ad7e61-19d3-4bb0-97bc-3aff2c5b3165"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("6536ca4a-c053-4d12-8fc6-f05dd210c0d2"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("222ebe94-dfd3-4632-95a4-4ecde30a8ee9"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val bloodSugarsForPatientInOtherFacilityInCurrentSyncGroup = listOf(
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("79c98115-4894-4fb7-8264-fb442a48b225"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("092de4a7-6493-4313-bfab-6bd2741ec143"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("cae126b9-515d-4056-8262-6d37b4a251e1"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val unsyncedBloodSugarMeasurementForPatientInOtherSyncGroup = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("ba9b72e9-da63-4d69-a023-0339a274c34e"),
        patientUuid = patientInOtherSyncGroup.patientUuid,
        facilityUuid = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.PENDING
    )
    val bloodSugarsForPatientInOtherSyncGroup = listOf(
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("75de9139-a7e0-4c55-9cb1-058b76e06da7"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("a3ac1607-fcee-4d8c-b2ea-f6c77b301a33"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.bloodSugarMeasurement(
            uuid = UUID.fromString("2b625418-16b1-46aa-97af-76f9245d9ece"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        unsyncedBloodSugarMeasurementForPatientInOtherSyncGroup
    )

    val allBloodSugarMeasurements = bloodSugarsForPatientInCurrentFacility +
        bloodSugarsForPatientInOtherFacilityInCurrentSyncGroup +
        bloodSugarsForPatientInOtherSyncGroup

    bloodSugarMeasurementDao.save(allBloodSugarMeasurements)
    assertThat(bloodSugarMeasurementDao.getAllBloodSugarMeasurements()).containsExactlyElementsIn(allBloodSugarMeasurements)

    // when
    database.deletePatientsNotInFacilitySyncGroup(currentFacility)

    // then
    val expectedBloodSugarMeasurements = bloodSugarsForPatientInCurrentFacility +
        bloodSugarsForPatientInOtherFacilityInCurrentSyncGroup +
        unsyncedBloodSugarMeasurementForPatientInOtherSyncGroup
    assertThat(bloodSugarMeasurementDao.getAllBloodSugarMeasurements()).containsExactlyElementsIn(expectedBloodSugarMeasurements)
  }

  @Test
  fun deleting_the_sync_group_data_should_delete_appointments_which_do_not_have_a_linked_patient() {
    // given
    val patientInCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("d1523ba6-bad3-42f2-a920-a503f1a503e3"),
        patientRegisteredFacilityId = currentFacility.uuid,
        patientAssignedFacilityId = currentFacility.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherFacilityInSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("cc131584-b88b-42b8-8f4c-29c93021765f"),
        patientRegisteredFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        patientAssignedFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("5cbe9277-d18a-49ad-a73b-1840a7aba0a9"),
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    patientRepository.save(listOf(
        patientInCurrentFacility,
        patientInOtherFacilityInSyncGroup,
        patientInOtherSyncGroup
    )).blockingAwait()

    val appointmentsForPatientInCurrentFacility = listOf(
        TestData.appointment(
            uuid = UUID.fromString("a8ad7e61-19d3-4bb0-97bc-3aff2c5b3165"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.appointment(
            uuid = UUID.fromString("6536ca4a-c053-4d12-8fc6-f05dd210c0d2"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.appointment(
            uuid = UUID.fromString("222ebe94-dfd3-4632-95a4-4ecde30a8ee9"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val appointmentsForPatientInOtherFacilityInCurrentSyncGroup = listOf(
        TestData.appointment(
            uuid = UUID.fromString("79c98115-4894-4fb7-8264-fb442a48b225"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.appointment(
            uuid = UUID.fromString("092de4a7-6493-4313-bfab-6bd2741ec143"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.appointment(
            uuid = UUID.fromString("cae126b9-515d-4056-8262-6d37b4a251e1"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val unsyncedAppointmentForPatientInOtherSyncGroup = TestData.appointment(
        uuid = UUID.fromString("ba9b72e9-da63-4d69-a023-0339a274c34e"),
        patientUuid = patientInOtherSyncGroup.patientUuid,
        facilityUuid = facilityInAnotherSyncGroup.uuid,
        status = Appointment.Status.Visited,
        cancelReason = null,
        syncStatus = SyncStatus.PENDING
    )
    val appointmentsForPatientInOtherSyncGroup = listOf(
        TestData.appointment(
            uuid = UUID.fromString("75de9139-a7e0-4c55-9cb1-058b76e06da7"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE,
            status = Appointment.Status.Visited,
            cancelReason = null
        ),
        TestData.appointment(
            uuid = UUID.fromString("a3ac1607-fcee-4d8c-b2ea-f6c77b301a33"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE,
            status = Appointment.Status.Visited,
            cancelReason = null
        ),
        TestData.appointment(
            uuid = UUID.fromString("2b625418-16b1-46aa-97af-76f9245d9ece"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE,
            status = Appointment.Status.Visited,
            cancelReason = null
        ),
        unsyncedAppointmentForPatientInOtherSyncGroup
    )

    val allAppointments = appointmentsForPatientInCurrentFacility +
        appointmentsForPatientInOtherFacilityInCurrentSyncGroup +
        appointmentsForPatientInOtherSyncGroup

    appointmentDao.save(allAppointments)
    assertThat(appointmentDao.getAllAppointments()).containsExactlyElementsIn(allAppointments)

    // when
    database.deletePatientsNotInFacilitySyncGroup(currentFacility)

    // then
    val expectedAppointments = appointmentsForPatientInCurrentFacility +
        appointmentsForPatientInOtherFacilityInCurrentSyncGroup +
        unsyncedAppointmentForPatientInOtherSyncGroup
    assertThat(appointmentDao.getAllAppointments()).containsExactlyElementsIn(expectedAppointments)
  }

  @Test
  fun deleting_the_sync_group_data_should_delete_prescribed_drugs_which_do_not_have_a_linked_patient() {
    // given
    val patientInCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("d1523ba6-bad3-42f2-a920-a503f1a503e3"),
        patientRegisteredFacilityId = currentFacility.uuid,
        patientAssignedFacilityId = currentFacility.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherFacilityInSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("cc131584-b88b-42b8-8f4c-29c93021765f"),
        patientRegisteredFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        patientAssignedFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("5cbe9277-d18a-49ad-a73b-1840a7aba0a9"),
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    patientRepository.save(listOf(
        patientInCurrentFacility,
        patientInOtherFacilityInSyncGroup,
        patientInOtherSyncGroup
    )).blockingAwait()

    val prescribedDrugsForPatientInCurrentFacility = listOf(
        TestData.prescription(
            uuid = UUID.fromString("a8ad7e61-19d3-4bb0-97bc-3aff2c5b3165"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.prescription(
            uuid = UUID.fromString("6536ca4a-c053-4d12-8fc6-f05dd210c0d2"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.prescription(
            uuid = UUID.fromString("222ebe94-dfd3-4632-95a4-4ecde30a8ee9"),
            patientUuid = patientInCurrentFacility.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val prescribedDrugsForPatientInOtherFacilityInCurrentSyncGroup = listOf(
        TestData.prescription(
            uuid = UUID.fromString("79c98115-4894-4fb7-8264-fb442a48b225"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.prescription(
            uuid = UUID.fromString("092de4a7-6493-4313-bfab-6bd2741ec143"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.prescription(
            uuid = UUID.fromString("cae126b9-515d-4056-8262-6d37b4a251e1"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val unsyncedPrescribedDrugForPatientInOtherSyncGroup = TestData.prescription(
        uuid = UUID.fromString("ba9b72e9-da63-4d69-a023-0339a274c34e"),
        patientUuid = patientInOtherSyncGroup.patientUuid,
        facilityUuid = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.PENDING
    )
    val prescribedDrugsForPatientInOtherSyncGroup = listOf(
        TestData.prescription(
            uuid = UUID.fromString("75de9139-a7e0-4c55-9cb1-058b76e06da7"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = currentFacility.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.prescription(
            uuid = UUID.fromString("a3ac1607-fcee-4d8c-b2ea-f6c77b301a33"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = otherFacilityInCurrentSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        TestData.prescription(
            uuid = UUID.fromString("2b625418-16b1-46aa-97af-76f9245d9ece"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            facilityUuid = facilityInAnotherSyncGroup.uuid,
            syncStatus = SyncStatus.DONE
        ),
        unsyncedPrescribedDrugForPatientInOtherSyncGroup
    )

    val allPrescribedDrugs = prescribedDrugsForPatientInCurrentFacility +
        prescribedDrugsForPatientInOtherFacilityInCurrentSyncGroup +
        prescribedDrugsForPatientInOtherSyncGroup

    prescribedDrugDao.save(allPrescribedDrugs)
    assertThat(prescribedDrugDao.getAllPrescribedDrugs()).containsExactlyElementsIn(allPrescribedDrugs)

    // when
    database.deletePatientsNotInFacilitySyncGroup(currentFacility)

    // then
    val expectedPrescribedDrugs = prescribedDrugsForPatientInCurrentFacility +
        prescribedDrugsForPatientInOtherFacilityInCurrentSyncGroup +
        unsyncedPrescribedDrugForPatientInOtherSyncGroup
    assertThat(prescribedDrugDao.getAllPrescribedDrugs()).containsExactlyElementsIn(expectedPrescribedDrugs)
  }

  @Test
  fun deleting_the_sync_group_data_should_delete_medical_histories_which_do_not_have_a_linked_patient() {
    // given
    val patientInCurrentFacility = TestData.patientProfile(
        patientUuid = UUID.fromString("d1523ba6-bad3-42f2-a920-a503f1a503e3"),
        patientRegisteredFacilityId = currentFacility.uuid,
        patientAssignedFacilityId = currentFacility.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherFacilityInSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("cc131584-b88b-42b8-8f4c-29c93021765f"),
        patientRegisteredFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        patientAssignedFacilityId = otherFacilityInCurrentSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    val patientInOtherSyncGroup = TestData.patientProfile(
        patientUuid = UUID.fromString("5cbe9277-d18a-49ad-a73b-1840a7aba0a9"),
        patientRegisteredFacilityId = facilityInAnotherSyncGroup.uuid,
        patientAssignedFacilityId = facilityInAnotherSyncGroup.uuid,
        syncStatus = SyncStatus.DONE
    )
    patientRepository.save(listOf(
        patientInCurrentFacility,
        patientInOtherFacilityInSyncGroup,
        patientInOtherSyncGroup
    )).blockingAwait()

    val medicalHistoryPatientInCurrentFacility = listOf(
        TestData.medicalHistory(
            uuid = UUID.fromString("a8ad7e61-19d3-4bb0-97bc-3aff2c5b3165"),
            patientUuid = patientInCurrentFacility.patientUuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val medicalHistoryForPatientInOtherFacilityInCurrentSyncGroup = listOf(
        TestData.medicalHistory(
            uuid = UUID.fromString("79c98115-4894-4fb7-8264-fb442a48b225"),
            patientUuid = patientInOtherFacilityInSyncGroup.patientUuid,
            syncStatus = SyncStatus.DONE
        )
    )

    val unsyncedMedicalHistoryForPatientInOtherSyncGroup = TestData.medicalHistory(
        uuid = UUID.fromString("ba9b72e9-da63-4d69-a023-0339a274c34e"),
        patientUuid = patientInOtherSyncGroup.patientUuid,
        syncStatus = SyncStatus.PENDING
    )
    val medicalHistoryForPatientInOtherSyncGroup = listOf(
        TestData.medicalHistory(
            uuid = UUID.fromString("75de9139-a7e0-4c55-9cb1-058b76e06da7"),
            patientUuid = patientInOtherSyncGroup.patientUuid,
            syncStatus = SyncStatus.DONE
        ),
        unsyncedMedicalHistoryForPatientInOtherSyncGroup
    )

    val allMedicalHistories = medicalHistoryPatientInCurrentFacility +
        medicalHistoryForPatientInOtherFacilityInCurrentSyncGroup +
        medicalHistoryForPatientInOtherSyncGroup

    medicalHistoryDao.save(allMedicalHistories)
    assertThat(medicalHistoryDao.getAllMedicalHistories()).containsExactlyElementsIn(allMedicalHistories)

    // when
    database.deletePatientsNotInFacilitySyncGroup(currentFacility)

    // then
    val expectedMedicalHistories = medicalHistoryPatientInCurrentFacility +
        medicalHistoryForPatientInOtherFacilityInCurrentSyncGroup +
        unsyncedMedicalHistoryForPatientInOtherSyncGroup
    assertThat(medicalHistoryDao.getAllMedicalHistories()).containsExactlyElementsIn(expectedMedicalHistories)
  }
}
