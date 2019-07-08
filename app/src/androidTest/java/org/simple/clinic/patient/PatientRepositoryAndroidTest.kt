package org.simple.clinic.patient

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.NO
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.YES
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.Appointment.AppointmentType
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.Appointment.Status.Visited
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.communication.Communication
import org.simple.clinic.overdue.communication.CommunicationRepository
import org.simple.clinic.patient.PatientStatus.ACTIVE
import org.simple.clinic.patient.PatientStatus.DEAD
import org.simple.clinic.patient.PatientStatus.INACTIVE
import org.simple.clinic.patient.PatientStatus.MIGRATED
import org.simple.clinic.patient.PatientStatus.UNRESPONSIVE
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.BusinessIdMetaData
import org.simple.clinic.patient.businessid.BusinessIdMetaDataAdapter
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unwrapJust
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

class PatientRepositoryAndroidTest {

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var bloodPressureRepository: BloodPressureRepository

  @Inject
  lateinit var prescriptionRepository: PrescriptionRepository

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  @Inject
  lateinit var communicationRepository: CommunicationRepository

  @Inject
  lateinit var medicalHistoryRepository: MedicalHistoryRepository

  @Inject
  lateinit var reportsRepository: ReportsRepository

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var clock: UtcClock

  @Inject
  lateinit var configProvider: Observable<PatientConfig>

  @Inject
  lateinit var businessIdMetaDataAdapter: BusinessIdMetaDataAdapter

  private val authenticationRule = AuthenticationRule()

  private val instantTaskExecutorRule = InstantTaskExecutorRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(instantTaskExecutorRule)
      .around(rxErrorsRule)!!

  val config: PatientConfig
    get() = configProvider.blockingFirst()

  private val testClock: TestUtcClock
    get() = clock as TestUtcClock

  private val loggedInUser: User
    get() = testData.qaUser()

  private val currentFacility: Facility
    get() = testData.qaFacility()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    testClock.setYear(2018)
  }

  @After
  fun tearDown() {
    (clock as TestUtcClock).resetToEpoch()
    reportsRepository.deleteReportsFile().blockingGet()
  }

  @Test
  fun when_a_patient_with_phone_numbers_is_saved_then_it_should_be_correctly_stored_in_the_database() {
    val ongoingAddress = OngoingNewPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails("Ashok Kumar", "08/04/1985", null, Gender.TRANSGENDER)
    val ongoingPhoneNumber = OngoingNewPatientEntry.PhoneNumber(number = "227788", type = PatientPhoneNumberType.LANDLINE)

    val personalDetailsOnlyEntry = OngoingNewPatientEntry(personalDetails = ongoingPersonalDetails)

    val savedPatient = patientRepository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(patientRepository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
        .flatMapCompletable { withAddressAndPhoneNumbers -> patientRepository.saveOngoingEntry(withAddressAndPhoneNumbers) }
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val patient = database.patientDao().getOne(savedPatient.uuid)!!

    assertThat(patient.dateOfBirth).isEqualTo(LocalDate.parse("1985-04-08"))
    assertThat(patient.age).isNull()

    val savedPhoneNumbers = database.phoneNumberDao().phoneNumber(patient.uuid).firstOrError().blockingGet()
    assertThat(savedPhoneNumbers).hasSize(1)
    assertThat(savedPhoneNumbers.first().number).isEqualTo("227788")
  }

  @Test
  fun when_a_patient_is_saved_then_its_searchable_name_should_also_be_added() {
    val names = arrayOf(
        "Riya Puri" to "RiyaPuri",
        "Manabi    Mehra" to "ManabiMehra",
        "Amit:Sodhi" to "AmitSodhi",
        "Riya.Puri" to "RiyaPuri",
        "Riya,Puri" to "RiyaPuri")

    names.forEach { (fullName, expectedSearchableName) ->
      val patientEntry = testData.ongoingPatientEntry(fullName = fullName)

      val patient = patientRepository.saveOngoingEntry(patientEntry)
          .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
          .blockingGet()

      assertThat(patient.searchableName).isEqualTo(expectedSearchableName)
    }
  }

  @Test
  fun when_a_patient_without_phone_numbers_is_saved_then_it_should_be_correctly_stored_in_the_database() {
    val patientEntry = testData.ongoingPatientEntry(fullName = "Jeevan Bima", phone = null)

    val savedPatient = patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val patient = database.patientDao().patient(savedPatient.uuid)

    assertThat(patient).isNotNull()

    val savedPhoneNumbers = database.phoneNumberDao().phoneNumber(savedPatient.uuid).firstOrError().blockingGet()
    assertThat(savedPhoneNumbers).isEmpty()
  }

  @Test
  fun when_a_patient_with_null_dateofbirth_and_nonnull_age_is_saved_then_it_should_be_correctly_stored_in_the_database() {
    val patientEntry = testData.ongoingPatientEntry(fullName = "Ashok Kumar")

    val savedPatient = patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val patient = database.patientDao().getOne(savedPatient.uuid)!!

    assertThat(patient.fullName).isEqualTo(patientEntry.personalDetails!!.fullName)
    assertThat(patient.dateOfBirth).isNull()
    assertThat(patient.age!!.value).isEqualTo(patientEntry.personalDetails!!.age!!.toInt())

    val savedPhoneNumbers = database.phoneNumberDao().phoneNumber(patient.uuid).firstOrError().blockingGet()

    assertThat(savedPhoneNumbers).hasSize(1)
    assertThat(savedPhoneNumbers.first().number).isEqualTo(patientEntry.phoneNumber!!.number)
  }

  @Test
  fun when_a_patient_with_null_dateofbirth_and_null_age_is_saved_then_it_should_not_be_accepted() {
    val patientEntry = testData.ongoingPatientEntry(dateOfBirth = null, age = null)

    patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .test()
        .assertError(AssertionError::class.java)
  }

  @Test
  fun when_a_patient_with_an_identifier_is_saved_then_it_should_be_correctly_saved_in_the_database() {
    val identifier = testData.identifier(value = "id", type = BpPassport)
    val patientEntry = testData.ongoingPatientEntry(identifier = identifier)

    val now = Instant.now(testClock)
    val advanceClockBy = Duration.ofDays(7L)
    testClock.advanceBy(advanceClockBy)

    val savedPatientUuid = patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()
        .uuid

    val patientProfile = database
        .patientDao()
        .patientProfile(savedPatientUuid)
        .blockingFirst()

    val savedBusinessId = patientProfile.businessIds.first()

    assertThat(savedBusinessId.identifier).isEqualTo(identifier)
    assertThat(savedBusinessId.createdAt).isEqualTo(now + advanceClockBy)
    assertThat(savedBusinessId.updatedAt).isEqualTo(now + advanceClockBy)
  }

  @Test
  fun when_a_patient_with_address_is_saved_then_search_should_correctly_return_combined_object() {
    val patientEntry = testData.ongoingPatientEntry(fullName = "Asha Kumar", dateOfBirth = "15/08/1947", age = null)

    patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .subscribe()

    val combinedPatient = patientRepository.search(name = "kumar", sortByFacility = currentFacility)
        .blockingFirst()
        .allPatientSearchResults()
        .first()

    assertThat(combinedPatient.fullName).isEqualTo("Asha Kumar")
    assertThat(combinedPatient.gender).isEqualTo(patientEntry.personalDetails!!.gender)
    assertThat(combinedPatient.dateOfBirth).isEqualTo(LocalDate.parse("1947-08-15"))
    assertThat(combinedPatient.createdAt).isAtLeast(combinedPatient.address.createdAt)
    assertThat(combinedPatient.syncStatus).isEqualTo(PENDING)
    assertThat(combinedPatient.address.colonyOrVillage).isEqualTo(patientEntry.address!!.colonyOrVillage)
    assertThat(combinedPatient.address.state).isEqualTo(patientEntry.address!!.state)
    assertThat(combinedPatient.phoneNumber).isNotEmpty()
    assertThat(combinedPatient.phoneNumber).isEqualTo(patientEntry.phoneNumber!!.number)
    assertThat(combinedPatient.phoneActive).isEqualTo(patientEntry.phoneNumber!!.active)
  }

  @Test
  fun when_patients_are_present_then_search_should_correctly_find_them() {
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails("Abhay Kumar", "15/08/1950", null, Gender.TRANSGENDER)
    val ongoingAddress = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val ongoingPhoneNumber = OngoingNewPatientEntry.PhoneNumber("3914159", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPatientEntry = OngoingNewPatientEntry(ongoingPersonalDetails, ongoingAddress, ongoingPhoneNumber)
    val abhayKumar = patientRepository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val opd2 = OngoingNewPatientEntry.PersonalDetails("Alok Kumar", "15/08/1940", null, Gender.TRANSGENDER)
    val opa2 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn2 = OngoingNewPatientEntry.PhoneNumber("3418959", PatientPhoneNumberType.MOBILE, active = true)
    val ope2 = OngoingNewPatientEntry(opd2, opa2, opn2)
    patientRepository.saveOngoingEntry(ope2)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val opd3 = OngoingNewPatientEntry.PersonalDetails("Abhishek Kumar", null, "68", Gender.TRANSGENDER)
    val opa3 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn3 = OngoingNewPatientEntry.PhoneNumber("9989159", PatientPhoneNumberType.MOBILE, active = true)
    val ope3 = OngoingNewPatientEntry(opd3, opa3, opn3)
    val abhishekKumar = patientRepository.saveOngoingEntry(ope3)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val opd4 = OngoingNewPatientEntry.PersonalDetails("Abshot Kumar", null, "67", Gender.TRANSGENDER)
    val opa4 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn4 = OngoingNewPatientEntry.PhoneNumber("1991591", PatientPhoneNumberType.MOBILE, active = true)
    val ope4 = OngoingNewPatientEntry(opd4, opa4, opn4)
    val abshotKumar = patientRepository.saveOngoingEntry(ope4)
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val search0 = patientRepository.search("Vinod", currentFacility).blockingFirst()
    assertThat(search0.allPatientSearchResults()).hasSize(0)

    val search1 = patientRepository.search("Alok", currentFacility).blockingFirst()
    val person1 = search1.allPatientSearchResults().first()
    assertThat(search1.allPatientSearchResults()).hasSize(1)
    assertThat(person1.fullName).isEqualTo("Alok Kumar")
    assertThat(person1.dateOfBirth).isEqualTo(LocalDate.parse("1940-08-15"))
    assertThat(person1.phoneNumber).isEqualTo("3418959")

    val search2 = patientRepository.search("ab", currentFacility).blockingFirst()
    val expectedResultsInSearch2 = setOf(abhayKumar, abhishekKumar, abshotKumar)

    assertThat(search2.allPatientSearchResults()).hasSize(expectedResultsInSearch2.size)
    search2.allPatientSearchResults().forEach { searchResult ->
      val expectedPatient = expectedResultsInSearch2.find { it.fullName == searchResult.fullName }!!

      assertThat(searchResult.fullName).isEqualTo(expectedPatient.fullName)
      assertThat(searchResult.dateOfBirth).isEqualTo(expectedPatient.dateOfBirth)
    }
  }

  @Test
  fun deleted_blood_pressures_should_be_excluded_when_searching_for_patients() {
    val now = Instant.now(clock)
    val currentFacility = facilityRepository.currentFacility(loggedInUser).blockingFirst()

    fun createPatientProfile(fullName: String): PatientProfile {
      return testData.patientProfile()
          .let { profile ->
            profile.copy(patient = profile.patient.copy(
                fullName = fullName,
                status = ACTIVE))
          }
    }

    fun createBp(patientUuid: UUID, recordedAt: Instant, deletedAt: Instant? = null): BloodPressureMeasurement {
      return testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = currentFacility.uuid,
          userUuid = loggedInUser.uuid,
          createdAt = now,
          deletedAt = deletedAt,
          recordedAt = recordedAt)
    }

    val patient0WithLatestBpDeleted = createPatientProfile(fullName = "Patient with latest BP deleted")
    val bpsForPatient0 = listOf(
        createBp(patient0WithLatestBpDeleted.patient.uuid, recordedAt = now.plusSeconds(2L)),
        createBp(patient0WithLatestBpDeleted.patient.uuid, recordedAt = now),
        createBp(patient0WithLatestBpDeleted.patient.uuid, recordedAt = now.plusSeconds(5L), deletedAt = now))
    patientRepository.save(listOf(patient0WithLatestBpDeleted))
        .andThen(bloodPressureRepository.save(bpsForPatient0))
        .blockingAwait()

    val patient1WithOneDeletedBp = createPatientProfile(fullName = "Patient with only one deleted BP")
    val bpsForPatient1 = listOf(
        createBp(patient1WithOneDeletedBp.patient.uuid, recordedAt = now, deletedAt = now))
    patientRepository.save(listOf(patient1WithOneDeletedBp))
        .andThen(bloodPressureRepository.save(bpsForPatient1))
        .blockingAwait()

    val patient2WithTwoDeletedBps = createPatientProfile(fullName = "Patient with two deleted BPs")
    val bpsForPatient2 = listOf(
        createBp(patient2WithTwoDeletedBps.patient.uuid, recordedAt = now, deletedAt = now),
        createBp(patient2WithTwoDeletedBps.patient.uuid, recordedAt = now.plusSeconds(1L), deletedAt = now))
    patientRepository.save(listOf(patient2WithTwoDeletedBps))
        .andThen(bloodPressureRepository.save(bpsForPatient2))
        .blockingAwait()

    val patient3WithNoBps = createPatientProfile(fullName = "Patient with no BPs")
    patientRepository.save(listOf(patient3WithNoBps)).blockingAwait()

    val searchResults = patientRepository.search("patient", currentFacility)
        .blockingFirst()
        .allPatientSearchResults()
        .groupBy { it.uuid }
        .mapValues { (_, results) -> results.first() }

    assertThat(searchResults.size).isEqualTo(4)
    assertThat(searchResults[patient0WithLatestBpDeleted.patient.uuid]!!.lastBp!!.takenOn).isEqualTo(now.plusSeconds(2L))
    assertThat(searchResults[patient1WithOneDeletedBp.patient.uuid]!!.lastBp).isNull()
    assertThat(searchResults[patient2WithTwoDeletedBps.patient.uuid]!!.lastBp).isNull()
    assertThat(searchResults[patient3WithNoBps.patient.uuid]!!.lastBp).isNull()
  }

  @Test
  fun when_the_patient_data_is_cleared_all_patient_data_must_be_cleared() {
    val facilityPayloads = listOf(testData.facilityPayload())
    val facilityUuid = facilityPayloads.first().uuid
    facilityRepository.mergeWithLocalData(facilityPayloads).blockingAwait()

    val user = testData.loggedInUser()
    database.userDao().createOrUpdate(user)
    database.userFacilityMappingDao().insertOrUpdate(user, listOf(facilityUuid))

    val patientPayloads = (1..2).map { testData.patientPayload() }

    val patientUuid = patientPayloads.first().uuid

    val rangeOfRecords = 1..4
    val bloodPressurePayloads = rangeOfRecords.map { testData.bpPayload(patientUuid = patientUuid, facilityUuid = facilityUuid) }
    val prescriptionPayloads = rangeOfRecords.map { testData.prescriptionPayload(patientUuid = patientUuid, facilityUuid = facilityUuid) }
    val appointmentPayloads = rangeOfRecords.map { testData.appointmentPayload(patientUuid = patientUuid) }

    val appointmentUuid = appointmentPayloads.first().uuid
    val communicationPayloads = rangeOfRecords.map { testData.communicationPayload(appointmentUuid = appointmentUuid) }
    val medicalHistoryPayloads = rangeOfRecords.map { testData.medicalHistoryPayload(patientUuid = patientUuid) }

    Completable.mergeArray(
        patientRepository.mergeWithLocalData(patientPayloads),
        bloodPressureRepository.mergeWithLocalData(bloodPressurePayloads),
        prescriptionRepository.mergeWithLocalData(prescriptionPayloads),
        appointmentRepository.mergeWithLocalData(appointmentPayloads),
        communicationRepository.mergeWithLocalData(communicationPayloads),
        medicalHistoryRepository.mergeWithLocalData(medicalHistoryPayloads)
    ).blockingAwait()

    reportsRepository.updateReports("test reports!").blockingAwait()
    val (reportsFile) = reportsRepository.reportsFile().blockingFirst()

    // We need to ensure that ONLY the tables related to the patient get cleared,
    // and the ones referring to the user must be left untouched

    assertThat(database.patientDao().patientCount().blockingFirst()).isGreaterThan(0)
    assertThat(database.addressDao().count()).isGreaterThan(0)
    assertThat(database.phoneNumberDao().count()).isGreaterThan(0)
    assertThat(database.businessIdDao().count()).isGreaterThan(0)
    assertThat(database.bloodPressureDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.prescriptionDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.facilityDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.userDao().userImmediate()).isNotNull()
    assertThat(database.userFacilityMappingDao().mappingsForUser(user.uuid).blockingFirst()).isNotEmpty()
    assertThat(database.appointmentDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.communicationDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.medicalHistoryDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(reportsFile!!.exists()).isTrue()

    patientRepository.clearPatientData().blockingAwait()

    assertThat(database.patientDao().patientCount().blockingFirst()).isEqualTo(0)
    assertThat(database.addressDao().count()).isEqualTo(0)
    assertThat(database.phoneNumberDao().count()).isEqualTo(0)
    assertThat(database.businessIdDao().count()).isEqualTo(0)
    assertThat(database.bloodPressureDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.prescriptionDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.appointmentDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.communicationDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.medicalHistoryDao().count().blockingFirst()).isEqualTo(0)
    assertThat(reportsFile.exists()).isFalse()

    assertThat(database.facilityDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.userDao().userImmediate()).isNotNull()
    assertThat(database.userFacilityMappingDao().mappingsForUser(user.uuid).blockingFirst()).isNotEmpty()
  }

  @Test
  fun patients_who_have_ever_visited_current_facility_should_be_present_at_the_top_when_searching() {
    val facilities = facilityRepository.facilities().blockingFirst()
    val currentFacility = facilityRepository.currentFacility(loggedInUser).blockingFirst()
    val otherFacility = facilities.first { it != currentFacility }

    facilityRepository.associateUserWithFacilities(loggedInUser, facilities.map { it.uuid }).blockingAwait()
    facilityRepository.setCurrentFacility(loggedInUser, currentFacility).blockingAwait()

    data class FacilityAndBloodPressureDeleted(val facility: Facility, val isBloodPressureDeleted: Boolean)

    val data = listOf(
        "Patient with one BP in other facility" to listOf(
            FacilityAndBloodPressureDeleted(otherFacility, false)),
        "Patient with one BP in current facility" to listOf(
            FacilityAndBloodPressureDeleted(currentFacility, false)),
        "Patient with two BPs in current facility" to listOf(
            FacilityAndBloodPressureDeleted(currentFacility, false),
            FacilityAndBloodPressureDeleted(currentFacility, false)),
        "Patient with two BPs, latest in current facility" to listOf(
            FacilityAndBloodPressureDeleted(otherFacility, false),
            FacilityAndBloodPressureDeleted(currentFacility, false)),
        "Patient with two BPs, latest in other facility" to listOf(
            FacilityAndBloodPressureDeleted(currentFacility, false),
            FacilityAndBloodPressureDeleted(otherFacility, false)),
        "Patient with two BPs, latest in other facility deleted, older in current facility" to listOf(
            FacilityAndBloodPressureDeleted(currentFacility, false),
            FacilityAndBloodPressureDeleted(otherFacility, true)),
        "Patient with two BPs, latest in current facility deleted, older in other facility" to listOf(
            FacilityAndBloodPressureDeleted(otherFacility, false),
            FacilityAndBloodPressureDeleted(currentFacility, true)),
        "Patient with no BPs" to listOf())

    data.forEach { (patientName, visitedFacilities) ->
      val patientProfile = testData.patientProfile()
          .let { profile ->
            profile.copy(patient = profile.patient.copy(fullName = patientName, status = ACTIVE))
          }

      patientRepository.save(listOf(patientProfile)).blockingAwait()

      // Record BPs in different facilities.
      visitedFacilities.forEach { (facility, isBloodPressureDeleted) ->
        val bloodPressureMeasurement = testData.bloodPressureMeasurement(
            patientUuid = patientProfile.patient.uuid,
            facilityUuid = facility.uuid,
            deletedAt = if (isBloodPressureDeleted) Instant.now() else null)

        bloodPressureRepository.save(listOf(bloodPressureMeasurement)).blockingAwait()
      }
    }

    val searchResults = patientRepository.search("patient", currentFacility).blockingFirst()
    assertThat(searchResults.allPatientSearchResults()).hasSize(data.size)

    val patientsWhoHaveVisitedCurrentFacility = setOf(
        "Patient with one BP in current facility",
        "Patient with two BPs in current facility",
        "Patient with two BPs, latest in current facility",
        "Patient with two BPs, latest in other facility deleted, older in current facility",
        "Patient with two BPs, latest in other facility")
    val patientsWhoHaveNeverVisitedCurrentFacility = setOf(
        "Patient with one BP in other facility",
        "Patient with no BPs",
        "Patient with two BPs, latest in current facility deleted, older in other facility")

    val findIndexOfPatientInSearchResults: (String) -> Int = { patientName ->
      searchResults.allPatientSearchResults().indexOfFirst { it.fullName == patientName }
    }
    val indicesOfVisitedCurrentFacilityPatientsInSearchResults = patientsWhoHaveVisitedCurrentFacility
        .map(findIndexOfPatientInSearchResults)
        .toSet()
    val indicesOfNotVisitedCurrentFacilityPatientsInSearchResults = patientsWhoHaveNeverVisitedCurrentFacility
        .map(findIndexOfPatientInSearchResults)
        .toSet()

    assertThat(indicesOfVisitedCurrentFacilityPatientsInSearchResults).isEqualTo(setOf(0, 1, 2, 3, 4))
    assertThat(indicesOfNotVisitedCurrentFacilityPatientsInSearchResults).isEqualTo(setOf(5, 6, 7))
  }

  @Test
  fun when_patient_is_marked_dead_they_should_not_show_in_search_results() {
    val patient = patientRepository
        .saveOngoingEntry(testData.ongoingPatientEntry(fullName = "Ashok Kumar"))
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    val searchResults = patientRepository.search(name = "Ashok", sortByFacility = currentFacility).blockingFirst()
    assertThat(searchResults.allPatientSearchResults()).isNotEmpty()
    assertThat(searchResults.allPatientSearchResults().first().fullName).isEqualTo("Ashok Kumar")

    patientRepository.updatePatientStatusToDead(patient.uuid).blockingAwait()

    val searchResultsAfterUpdate = patientRepository.search(name = "Ashok", sortByFacility = currentFacility).blockingFirst()
    assertThat(patientRepository.recordCount().blockingFirst()).isEqualTo(1)
    assertThat(searchResultsAfterUpdate.allPatientSearchResults()).isEmpty()

    val deadPatient: Patient = patientRepository.patient(patient.uuid)
        .unwrapJust()
        .blockingFirst()

    assertThat(patientRepository.recordCount().blockingFirst()).isEqualTo(1)
    assertThat(deadPatient.status).isEqualTo(DEAD)
  }

  @Test
  fun when_patient_is_marked_dead_they_should_marked_as_pending_sync() {
    val timeOfCreation = Instant.now(testClock)

    val patient = patientRepository
        .saveOngoingEntry(testData.ongoingPatientEntry(fullName = "Ashok Kumar"))
        .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
        .blockingGet()

    testClock.advanceBy(Duration.ofDays(365))
    val timeOfDeath = Instant.now(testClock)

    patientRepository.updatePatientStatusToDead(patient.uuid).blockingAwait()
    val deadPatient: Patient = patientRepository.patient(patient.uuid)
        .unwrapJust()
        .blockingFirst()

    assertThat(deadPatient.syncStatus).isEqualTo(PENDING)
    assertThat(deadPatient.updatedAt).isNotEqualTo(timeOfCreation)
    assertThat(deadPatient.updatedAt).isEqualTo(timeOfDeath)
  }

  /**
   * Added to test the case where SQLite's max query param length (999) can be
   * exceeded during fuzzy name search.
   */
  @Test
  fun when_searching_with_fuzzy_search_the_results_must_be_limited_to_the_value_set_in_the_config() {
    val template = testData.patientProfile(syncStatus = DONE)

    val patientsToSave = (1..PatientConfig.MAXIMUM_SQLITE_QUERY_LIMIT).map {
      val addressUuid = UUID.randomUUID()
      val patientUuid = UUID.randomUUID()

      template.copy(
          patient = template.patient.copy(
              uuid = patientUuid,
              addressUuid = addressUuid,
              fullName = "Name",
              searchableName = "Name",
              dateOfBirth = LocalDate.now(clock).minusYears(10),
              status = ACTIVE
          ),
          address = template.address.copy(uuid = addressUuid),
          phoneNumbers = template.phoneNumbers
              .map { number -> number.copy(uuid = UUID.randomUUID(), patientUuid = patientUuid) },
          businessIds = template.businessIds
              .map { businessId -> businessId.copy(uuid = UUID.randomUUID(), patientUuid = patientUuid) }
      )
    }

    patientRepository.save(patientsToSave).blockingAwait()
    assertThat(patientRepository.recordCount().blockingFirst()).isEqualTo(1000)

    assertThat(
        patientRepository
            .search(name = "ame", sortByFacility = currentFacility)
            .blockingFirst()
            .allPatientSearchResults()
            .size
    ).isEqualTo(config.limitOfSearchResults)
  }

  @Test
  fun editing_a_patients_phone_number_should_not_trigger_foreign_key_cascades_action() {
    database.openHelper.writableDatabase.setForeignKeyConstraintsEnabled(true)

    val patientUuid = UUID.randomUUID()
    val initialNumber = testData.phoneNumberPayload(number = "123")
    val initialPatient = testData.patientPayload(uuid = patientUuid, phoneNumbers = listOf(initialNumber))
    patientRepository.mergeWithLocalData(listOf(initialPatient)).blockingAwait()

    val updatedNumber = initialNumber.copy(number = "456")
    val updatedPatient = initialPatient.copy(phoneNumbers = listOf(updatedNumber))
    patientRepository.mergeWithLocalData(listOf(updatedPatient)).blockingAwait()

    assertThat(database.phoneNumberDao().count()).isEqualTo(1)

    database.patientDao().save(updatedPatient.toDatabaseModel(DONE))

    val storedNumbers = database.phoneNumberDao().phoneNumber(patientUuid).blockingFirst()
    assertThat(storedNumbers.size).isEqualTo(1)
    assertThat(storedNumbers[0].uuid).isEqualTo(updatedNumber.uuid)
    assertThat(storedNumbers[0].number).isEqualTo(updatedNumber.number)

    assertThat(database.openHelper.writableDatabase.isDatabaseIntegrityOk).isTrue()
  }

  @Test
  fun editing_a_patients_address_should_not_trigger_foreign_key_cascades_action() {
    database.openHelper.writableDatabase.setForeignKeyConstraintsEnabled(true)

    val patientUuid = UUID.randomUUID()
    val initialAddress = testData.addressPayload(district = "Gotham")
    val initialPatient = testData.patientPayload(uuid = patientUuid, address = initialAddress)
    patientRepository.mergeWithLocalData(listOf(initialPatient)).blockingAwait()

    assertThat(database.addressDao().count()).isEqualTo(1)
    assertThat(database.patientDao().patientCount().blockingFirst()).isEqualTo(1)

    val updatedAddress = initialAddress.copy(district = "Death Star")
    database.addressDao().save(updatedAddress.toDatabaseModel())

    assertThat(database.patientDao().patientCount().blockingFirst()).isEqualTo(1)

    val storedPatients = database.patientDao().patient(initialPatient.uuid).blockingFirst()
    assertThat(storedPatients.size).isEqualTo(1)
    assertThat(storedPatients[0].uuid).isEqualTo(patientUuid)

    assertThat(database.openHelper.writableDatabase.isDatabaseIntegrityOk).isTrue()
  }

  @Test
  fun editing_a_patients_profile_should_not_trigger_foreign_key_cascades_action() {
    database.openHelper.writableDatabase.setForeignKeyConstraintsEnabled(true)

    val patientUuid = UUID.randomUUID()
    val address = testData.addressPayload()
    val number = testData.phoneNumberPayload()
    val initialPatient = testData.patientPayload(
        uuid = patientUuid,
        fullName = "Scarecrow",
        address = address,
        phoneNumbers = listOf(number))
    patientRepository.mergeWithLocalData(listOf(initialPatient)).blockingAwait()

    assertThat(database.phoneNumberDao().count()).isEqualTo(1)
    assertThat(database.addressDao().count()).isEqualTo(1)

    val updatedPatient = initialPatient.copy()
    database.patientDao().save(updatedPatient.toDatabaseModel(DONE))

    val storedNumbers = database.phoneNumberDao().phoneNumber(patientUuid).blockingFirst()
    assertThat(database.phoneNumberDao().count()).isEqualTo(1)
    assertThat(storedNumbers.size).isEqualTo(1)
    assertThat(storedNumbers[0].uuid).isEqualTo(number.uuid)

    val storedAddresses = database.addressDao().address(address.uuid).blockingFirst()
    assertThat(database.addressDao().count()).isEqualTo(1)
    assertThat(storedAddresses.size).isEqualTo(1)
    assertThat(storedAddresses[0].uuid).isEqualTo(address.uuid)
  }

  @Test
  fun when_patient_address_is_updated_the_address_must_be_saved() {
    val addressToSave = testData.patientAddress(
        colonyOrVilage = "Old Colony",
        district = "Old District",
        state = "Old State",
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock)
    )

    val patientProfile = PatientProfile(
        patient = testData.patient(
            addressUuid = addressToSave.uuid,
            syncStatus = DONE
        ),
        address = addressToSave,
        phoneNumbers = emptyList(),
        businessIds = emptyList()
    )

    val patient = patientProfile.patient

    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestUtcClock).advanceBy(updatedAfter)

    val oldSavedAddress = patientRepository.address(patient.addressUuid)
        .unwrapJust()
        .blockingFirst()

    val newAddressToSave = oldSavedAddress.copy(
        colonyOrVillage = "New Colony",
        district = "New District",
        state = "New State"
    )

    patientRepository.updateAddressForPatient(patientUuid = patient.uuid, patientAddress = newAddressToSave).blockingAwait()

    val updatedPatient = patientRepository.patient(patient.uuid)
        .unwrapJust()
        .blockingFirst()

    assertThat(updatedPatient.syncStatus).isEqualTo(PENDING)

    val savedAddress = patientRepository.address(updatedPatient.addressUuid)
        .unwrapJust()
        .blockingFirst()

    assertThat(savedAddress.updatedAt).isEqualTo(oldSavedAddress.updatedAt.plus(updatedAfter))
    assertThat(savedAddress.createdAt).isNotEqualTo(savedAddress.updatedAt)
    assertThat(savedAddress.colonyOrVillage).isEqualTo("New Colony")
    assertThat(savedAddress.district).isEqualTo("New District")
    assertThat(savedAddress.state).isEqualTo("New State")
  }

  @Test
  fun when_patient_is_updated_the_patient_must_be_saved() {
    val addressToSave = testData.patientAddress(
        colonyOrVilage = "Old Colony",
        district = "Old District",
        state = "Old State"
    )

    val originalSavedPatient = testData.patient(
        syncStatus = DONE,
        addressUuid = addressToSave.uuid,
        fullName = "Old Name",
        gender = Gender.MALE,
        age = Age(value = 30, updatedAt = Instant.now(clock), computedDateOfBirth = LocalDate.now(clock)),
        dateOfBirth = LocalDate.now(clock),
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock)
    )

    val patientProfile = PatientProfile(
        patient = originalSavedPatient,
        address = addressToSave,
        phoneNumbers = listOf(
            testData.patientPhoneNumber(patientUuid = originalSavedPatient.uuid),
            testData.patientPhoneNumber(patientUuid = originalSavedPatient.uuid)
        ),
        businessIds = emptyList()
    )

    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestUtcClock).advanceBy(updatedAfter)

    val newPatientToSave = originalSavedPatient.copy(
        fullName = "New Name",
        gender = Gender.TRANSGENDER,
        age = Age(value = 35, updatedAt = Instant.now(clock), computedDateOfBirth = LocalDate.now(clock)),
        dateOfBirth = LocalDate.now(clock)
    )

    patientRepository.updatePatient(newPatientToSave).blockingAwait()

    val savedPatient = patientRepository.patient(newPatientToSave.uuid)
        .unwrapJust()
        .blockingFirst()

    assertThat(savedPatient.syncStatus).isEqualTo(PENDING)
    assertThat(savedPatient.updatedAt).isEqualTo(originalSavedPatient.updatedAt.plus(updatedAfter))
    assertThat(savedPatient.createdAt).isNotEqualTo(savedPatient.updatedAt)

    assertThat(savedPatient.fullName).isEqualTo("New Name")
    assertThat(savedPatient.searchableName).isEqualTo("NewName")
    assertThat(savedPatient.gender).isEqualTo(Gender.TRANSGENDER)
  }

  @Test
  fun when_phone_number_is_updated_it_should_be_saved() {
    val addressToSave = testData.patientAddress()

    val originalSavedPatient = testData.patient(
        syncStatus = DONE,
        addressUuid = addressToSave.uuid
    )

    val patientProfile = PatientProfile(
        patient = originalSavedPatient,
        address = addressToSave,
        phoneNumbers = listOf(
            testData.patientPhoneNumber(
                patientUuid = originalSavedPatient.uuid,
                number = "111111111",
                phoneType = PatientPhoneNumberType.LANDLINE,
                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock)
            ),
            testData.patientPhoneNumber(
                patientUuid = originalSavedPatient.uuid,
                number = "2222222222",
                phoneType = PatientPhoneNumberType.MOBILE,
                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock)
            )
        ),
        businessIds = emptyList()
    )

    patientRepository.save(listOf(patientProfile))
        .blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestUtcClock).advanceBy(updatedAfter)

    val phoneNumberToUpdate = patientProfile.phoneNumbers[1].copy(number = "12345678", phoneType = PatientPhoneNumberType.LANDLINE)

    patientRepository.updatePhoneNumberForPatient(originalSavedPatient.uuid, phoneNumberToUpdate).blockingAwait()

    val phoneNumbersSaved = database.phoneNumberDao()
        .phoneNumber(originalSavedPatient.uuid)
        .firstOrError()
        .blockingGet()

    val phoneNumber = phoneNumbersSaved.find { it.uuid == phoneNumberToUpdate.uuid }

    assertThat(phoneNumber).isNotNull()
    assertThat(phoneNumber!!.number).isEqualTo("12345678")
    assertThat(phoneNumber.phoneType).isEqualTo(PatientPhoneNumberType.LANDLINE)
    assertThat(phoneNumber.updatedAt).isEqualTo(patientProfile.phoneNumbers[1].updatedAt.plus(updatedAfter))
    assertThat(phoneNumber.updatedAt).isNotEqualTo(phoneNumber.createdAt)

    val patient = patientRepository.patient(originalSavedPatient.uuid)
        .unwrapJust()
        .blockingFirst()

    assertThat(patient.syncStatus).isEqualTo(PENDING)
  }

  @Test
  fun phone_number_should_be_saved_properly() {
    val addressToSave = testData.patientAddress()

    val originalSavedPatient = testData.patient(
        syncStatus = DONE,
        addressUuid = addressToSave.uuid
    )

    val patientProfile = PatientProfile(
        patient = originalSavedPatient,
        address = addressToSave,
        phoneNumbers = listOf(
            testData.patientPhoneNumber(
                patientUuid = originalSavedPatient.uuid,
                number = "111111111",
                phoneType = PatientPhoneNumberType.LANDLINE,
                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock))),
        businessIds = emptyList())

    patientRepository.save(listOf(patientProfile))
        .blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestUtcClock).advanceBy(updatedAfter)

    patientRepository.createPhoneNumberForPatient(
        patientUuid = originalSavedPatient.uuid,
        number = "2222222222",
        phoneNumberType = PatientPhoneNumberType.MOBILE,
        active = true
    ).blockingAwait()

    val phoneNumbersSaved = database.phoneNumberDao()
        .phoneNumber(originalSavedPatient.uuid)
        .firstOrError()
        .blockingGet()

    assertThat(phoneNumbersSaved.size).isEqualTo(2)

    val savedPhoneNumber = phoneNumbersSaved.find { it != patientProfile.phoneNumbers[0] }!!

    assertThat(savedPhoneNumber.active).isTrue()
    assertThat(savedPhoneNumber.createdAt).isEqualTo(Instant.now(clock))
    assertThat(savedPhoneNumber.createdAt).isEqualTo(savedPhoneNumber.updatedAt)
    assertThat(savedPhoneNumber.number).isEqualTo("2222222222")
    assertThat(savedPhoneNumber.phoneType).isEqualTo(PatientPhoneNumberType.MOBILE)

    val patient = patientRepository.patient(originalSavedPatient.uuid)
        .unwrapJust()
        .blockingFirst()

    assertThat(patient.syncStatus).isEqualTo(PENDING)
  }

  @Test
  fun verify_recent_patients_are_retrieved_as_expected() {
    var recentPatient1 = savePatientWithBpWithTestClock()

    verifyRecentPatientOrder(
        recentPatient1
    )

    testClock.advanceBy(Duration.ofSeconds(1))

    var recentPatient2 = savePatientWithBpWithTestClock()

    verifyRecentPatientOrder(
        recentPatient2,
        recentPatient1
    )

    testClock.advanceBy(Duration.ofSeconds(1))

    prescriptionRepository.savePrescription(recentPatient1.uuid, testData.protocolDrug(), currentFacility).blockingAwait()

    recentPatient1 = recentPatient1.copy(updatedAt = testClock.instant())
    verifyRecentPatientOrder(
        recentPatient1,
        recentPatient2
    )

    testClock.advanceBy(Duration.ofSeconds(1))

    val appointment2 = testData.appointment(
        patientUuid = recentPatient2.uuid,
        createdAt = testClock.instant(),
        updatedAt = testClock.instant().plusSeconds(1),
        status = Scheduled,
        appointmentType = Manual,
        cancelReason = null
    )
    appointmentRepository.save(listOf(appointment2)).blockingAwait()

    recentPatient2 = recentPatient2.copy(updatedAt = testClock.instant())
    verifyRecentPatientOrder(
        recentPatient2,
        recentPatient1
    )

    testClock.advanceBy(Duration.ofSeconds(1))

    medicalHistoryRepository.save(testData.medicalHistory(
        patientUuid = recentPatient1.uuid,
        updatedAt = testClock.instant()
    )) { Instant.now(testClock) }.blockingAwait()

    verifyRecentPatientOrder(
        recentPatient2,
        recentPatient1
    )

    testClock.advanceBy(Duration.ofSeconds(1))

    val recentPatient3 = savePatientWithBpWithTestClock()

    verifyRecentPatientOrder(
        recentPatient3,
        recentPatient2,
        recentPatient1
    )

    testClock.advanceBy(Duration.ofSeconds(1))

    val recentPatient4 = savePatientWithBpWithTestClock()

    verifyRecentPatientOrder(
        recentPatient4,
        recentPatient3,
        recentPatient2
    )
  }

  @Test
  fun verify_recent_patients_from_other_facilities_are_not_retrieved() {
    val facility1Uuid = UUID.randomUUID()
    val patient1InFacility1 = savePatientWithBp(facilityUuid = facility1Uuid)

    verifyRecentPatientOrder(patient1InFacility1, facilityUuid = facility1Uuid)

    val facility2Uuid = UUID.randomUUID()
    val patient1InFacility2 = savePatientWithBp(facilityUuid = facility2Uuid)

    verifyRecentPatientOrder(patient1InFacility1, facilityUuid = facility1Uuid)
    verifyRecentPatientOrder(patient1InFacility2, facilityUuid = facility2Uuid)

    val patient2InFacility1 = savePatientWithBp(facilityUuid = facility1Uuid)

    verifyRecentPatientOrder(patient2InFacility1, patient1InFacility1, facilityUuid = facility1Uuid)
  }

  @Test
  fun verify_deleted_bps_are_not_included_when_fetching_recent_patients() {
    val facilityUuid = testData.qaUserFacilityUuid()
    val recentPatient1 = savePatientWithBp(facilityUuid = facilityUuid)
    savePatientWithBp(facilityUuid = facilityUuid, deletedAt = Instant.now())
    val recentPatient3 = savePatientWithBp(facilityUuid = facilityUuid)

    val recentPatients = patientRepository
        .recentPatients(facilityUuid)
        .blockingFirst()
    assertThat(recentPatients).isEqualTo(listOf(recentPatient3, recentPatient1))
  }

  private fun savePatientWithBp(
      facilityUuid: UUID = testData.qaUserFacilityUuid(),
      patientUuid: UUID = UUID.randomUUID(),
      patientAddressUuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now(),
      patientStatus: PatientStatus = ACTIVE
  ): RecentPatient {
    val patientProfile = testData.patientProfile(patientUuid = patientUuid, patientAddressUuid = patientAddressUuid).run {
      copy(patient = patient.copy(status = patientStatus))
    }
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val bpMeasurement = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt
    )
    database.bloodPressureDao().save(listOf(bpMeasurement))
    return patientProfile.patient.toRecentPatient(bpMeasurement)
  }

  private fun savePatientWithBpWithTestClock(
      facilityUuid: UUID = testData.qaUserFacilityUuid(),
      patientUuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(testClock),
      updatedAt: Instant = Instant.now(testClock),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now(testClock)
  ): RecentPatient {
    val patientProfile = testData.patientProfile(patientUuid = patientUuid).run {
      copy(patient = patient.copy(createdAt = createdAt, updatedAt = updatedAt))
    }
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val bpMeasurement = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt
    )
    database.bloodPressureDao().save(listOf(bpMeasurement))
    return patientProfile.patient.toRecentPatient(bpMeasurement)
  }

  private fun Patient.toRecentPatient(bpMeasurement: BloodPressureMeasurement) = RecentPatient(
      uuid = uuid,
      fullName = fullName,
      gender = gender,
      dateOfBirth = dateOfBirth,
      age = age,
      updatedAt = bpMeasurement.recordedAt
  )

  private fun verifyRecentPatientOrder(
      vararg expectedRecentPatients: RecentPatient,
      facilityUuid: UUID = testData.qaUserFacilityUuid()
  ) {
    val recentPatients = patientRepository
        .recentPatients(facilityUuid, limit = 3)
        .blockingFirst()
    assertThat(recentPatients).isEqualTo(expectedRecentPatients.toList())
  }

  @Test
  fun verify_deleted_prescribed_drugs_are_not_included_when_fetching_recent_patients() {
    val facilityUuid = testData.qaUserFacilityUuid()
    val recentPatient1 = savePatientWithPrescribedDrug(facilityUuid = facilityUuid)
    val recentPatient2 = savePatientWithPrescribedDrug(facilityUuid = facilityUuid, deletedAt = Instant.now())
    val recentPatient3 = savePatientWithPrescribedDrug(facilityUuid = facilityUuid)

    val recentPatients = patientRepository
        .recentPatients(facilityUuid)
        .blockingFirst()
    assertThat(recentPatients).isEqualTo(listOf(recentPatient3, recentPatient1))
  }

  private fun savePatientWithPrescribedDrug(
      facilityUuid: UUID = testData.qaUserFacilityUuid(),
      patientUuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): RecentPatient {
    val patientProfile = testData.patientProfile(patientUuid = patientUuid)
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val prescribedDrug = testData.prescription(
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
    database.prescriptionDao().save(listOf(prescribedDrug))
    return patientProfile.patient.run {
      RecentPatient(
          uuid = uuid,
          fullName = fullName,
          gender = gender,
          dateOfBirth = dateOfBirth,
          age = age,
          updatedAt = updatedAt
      )
    }
  }

  @Test
  fun verify_deleted_appointments_are_not_included_when_fetching_recent_patients() {
    val facilityUuid = UUID.randomUUID()
    val recentPatient1 = savePatientWithAppointment(facilityUuid = facilityUuid)
    val recentPatient2 = savePatientWithAppointment(facilityUuid = facilityUuid, deletedAt = Instant.now())
    val recentPatient3 = savePatientWithAppointment(facilityUuid = facilityUuid)

    val recentPatients = patientRepository
        .recentPatients(facilityUuid)
        .blockingFirst()
    assertThat(recentPatients).isEqualTo(listOf(recentPatient3, recentPatient1))
  }

  @Test
  fun verify_only_scheduled_appointments_are_included_when_fetching_recent_patients() {
    val facilityUuid = UUID.randomUUID()
    val recentPatient1 = savePatientWithAppointment(facilityUuid = facilityUuid, status = Scheduled)
    val recentPatient2 = savePatientWithAppointment(facilityUuid = facilityUuid, status = Cancelled)
    val recentPatient3 = savePatientWithAppointment(facilityUuid = facilityUuid, status = Visited)

    val recentPatients = patientRepository
        .recentPatients(facilityUuid)
        .blockingFirst()
    assertThat(recentPatients).isEqualTo(listOf(recentPatient1))
  }

  @Test
  fun verify_only_patients_with_manual_appointments_are_included_when_fetching_recent_patients() {
    val facilityUuid = UUID.randomUUID()
    val recentPatient1 = savePatientWithAppointment(facilityUuid = facilityUuid, appointmentType = Automatic)
    val recentPatient2 = savePatientWithAppointment(facilityUuid = facilityUuid, appointmentType = Manual)
    val recentPatient3 = savePatientWithAppointment(facilityUuid = facilityUuid, appointmentType = AppointmentType.Unknown(""))

    val recentPatients = patientRepository
        .recentPatients(facilityUuid)
        .blockingFirst()
    assertThat(recentPatients).isEqualTo(listOf(recentPatient2))
  }

  private fun savePatientWithAppointment(
      appointmentUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = testData.qaUserFacilityUuid(),
      patientUuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      status: Status = Scheduled,
      appointmentType: AppointmentType = Manual
  ): RecentPatient {
    val patientProfile = testData.patientProfile(patientUuid = patientUuid)
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val appointment = testData.appointment(
        uuid = appointmentUuid,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        status = status,
        appointmentType = appointmentType
    )
    database.appointmentDao().save(listOf(appointment))
    return patientProfile.patient.run {
      RecentPatient(
          uuid = uuid,
          fullName = fullName,
          gender = gender,
          dateOfBirth = dateOfBirth,
          age = age,
          updatedAt = createdAt
      )
    }
  }

  @Test
  fun verify_communications_do_not_affect_recent_patients() {
    val facilityUuid = UUID.randomUUID()
    val appointmentUuid1 = UUID.randomUUID()
    val recentPatient1 = savePatientWithAppointment(
        appointmentUuid = appointmentUuid1,
        facilityUuid = facilityUuid
    )
    val appointmentUuid2 = UUID.randomUUID()
    val recentPatient2 = savePatientWithAppointment(
        appointmentUuid = appointmentUuid2,
        facilityUuid = facilityUuid
    )
    val appointmentUuid3 = UUID.randomUUID()
    val recentPatient3 = savePatientWithAppointment(
        appointmentUuid = appointmentUuid3,
        facilityUuid = facilityUuid
    )

    saveCommunication(appointmentUuid = appointmentUuid1)
    saveCommunication(appointmentUuid = appointmentUuid2)
    saveCommunication(
        appointmentUuid = appointmentUuid3,
        deletedAt = Instant.now()
    )

    val recentPatients = patientRepository
        .recentPatients(facilityUuid)
        .blockingFirst()
    assertThat(recentPatients).isEqualTo(listOf(
        recentPatient3,
        recentPatient2,
        recentPatient1
    ))
  }

  private fun saveCommunication(
      appointmentUuid: UUID,
      deletedAt: Instant? = null
  ): Communication {
    val communication = testData.communication(
        appointmentUuid = appointmentUuid,
        deletedAt = deletedAt
    )
    database.communicationDao().save(listOf(communication))
    return communication
  }

  @Test
  fun if_sync_is_pending_for_any_patient_record_then_it_should_be_counted_in_pendingRecordsCount() {
    patientRepository.save(listOf(
        testData.patientProfile(syncStatus = PENDING),
        testData.patientProfile(syncStatus = PENDING),
        testData.patientProfile(syncStatus = DONE)
    )).blockingAwait()

    val count = patientRepository.pendingSyncRecordCount().blockingFirst()
    assertThat(count).isEqualTo(2)
  }

  @Test
  fun saving_an_identifier_for_a_patient_must_work_as_expected() {
    val patientProfile = testData.patientProfile(syncStatus = DONE, generateBusinessId = false)
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val bpPassportCode = UUID.randomUUID().toString()
    val now = Instant.now(clock)

    val duration = Duration.ofDays(1L)
    testClock.advanceBy(duration)

    val savedBusinessId = patientRepository
        .addIdentifierToPatient(
            patientUuid = patientProfile.patient.uuid,
            identifier = Identifier(bpPassportCode, BpPassport),
            assigningUser = loggedInUser,
            assigningFacility = currentFacility
        )
        .blockingGet()

    assertThat(savedBusinessId.uuid).isNotEqualTo(bpPassportCode)
    assertThat(savedBusinessId.patientUuid).isEqualTo(patientProfile.patient.uuid)
    assertThat(savedBusinessId.identifier)
        .isEqualTo(Identifier(value = bpPassportCode, type = BpPassport))
    assertThat(savedBusinessId.metaDataVersion).isEqualTo(BusinessId.MetaDataVersion.BpPassportMetaDataV1)
    assertThat(savedBusinessId.createdAt).isEqualTo(now.plus(duration))
    assertThat(savedBusinessId.updatedAt).isEqualTo(now.plus(duration))
    assertThat(savedBusinessId.deletedAt).isNull()

    val savedMeta = businessIdMetaDataAdapter.deserialize(savedBusinessId.metaData, BusinessId.MetaDataVersion.BpPassportMetaDataV1)
    val expectedSavedMeta = BusinessIdMetaData.BpPassportMetaDataV1(
        assigningUserUuid = loggedInUser.uuid,
        assigningFacilityUuid = currentFacility.uuid
    )
    assertThat(savedMeta).isEqualTo(expectedSavedMeta)

    val (updatedPatient) = patientRepository.patient(patientProfile.patient.uuid).blockingFirst() as Just
    assertThat(updatedPatient.syncStatus).isEqualTo(PENDING)
  }

  @Test
  fun finding_a_patient_by_a_business_id_must_work_as_expected() {
    val patientProfileTemplate = testData.patientProfile(syncStatus = DONE, generateBusinessId = false, generatePhoneNumber = false)

    val uniqueBusinessIdentifier = "unique_business_id"
    val sharedBusinessIdentifier = "shared_business_id"
    val deletedBusinessIdentifier = "deleted_business_id"

    val identifierType = Unknown("test_identifier")
    val metaVersion = BusinessId.MetaDataVersion.Unknown("test_version")
    val now = Instant.now(clock)

    val patientWithUniqueBusinessId = patientProfileTemplate.let { patientProfile ->
      val addressUuid = UUID.randomUUID()
      val patientUuid = UUID.randomUUID()

      val patient = patientProfile.patient.copy(
          uuid = patientUuid,
          fullName = "Patient with unique business ID",
          addressUuid = addressUuid
      )
      val address = patientProfile.address.copy(uuid = addressUuid)
      val businessId = BusinessId(
          uuid = UUID.randomUUID(),
          patientUuid = patientUuid,
          identifier = Identifier(uniqueBusinessIdentifier, identifierType),
          metaDataVersion = metaVersion,
          metaData = "",
          createdAt = now,
          updatedAt = now,
          deletedAt = null
      )

      patientProfile.copy(patient = patient, address = address, businessIds = listOf(businessId))
    }

    val (patientOneWithSharedBusinessId, patientTwoWithSharedBusinessId) = patientProfileTemplate.let { patientProfile ->
      val patientUuidOne = UUID.randomUUID()
      val addressUuidOne = UUID.randomUUID()
      val patientOne = patientProfile.patient.copy(
          uuid = patientUuidOne,
          fullName = "Patient one with shared business ID",
          addressUuid = addressUuidOne,
          createdAt = now.minusSeconds(1),
          updatedAt = now.minusSeconds(1)
      )
      val addressOne = patientProfile.address.copy(uuid = addressUuidOne)
      val businessIdOne = BusinessId(
          uuid = UUID.randomUUID(),
          patientUuid = patientUuidOne,
          identifier = Identifier(sharedBusinessIdentifier, identifierType),
          metaDataVersion = metaVersion,
          metaData = "",
          createdAt = now,
          updatedAt = now,
          deletedAt = null
      )
      val patientProfileOne = patientProfile.copy(patient = patientOne, address = addressOne, businessIds = listOf(businessIdOne))

      val patientUuidTwo = UUID.randomUUID()
      val addressUuidTwo = UUID.randomUUID()
      val patientTwo = patientProfile.patient.copy(
          fullName = "Patient two with shared business ID",
          uuid = patientUuidTwo,
          addressUuid = addressUuidTwo,
          createdAt = now.plusSeconds(1),
          updatedAt = now.plusSeconds(1)
      )
      val addressTwo = patientProfile.address.copy(uuid = addressUuidTwo)
      val businessIdTwo = BusinessId(
          uuid = UUID.randomUUID(),
          patientUuid = patientUuidTwo,
          identifier = Identifier(sharedBusinessIdentifier, identifierType),
          metaDataVersion = metaVersion,
          metaData = "",
          createdAt = now.minusSeconds(1),
          updatedAt = now.minusSeconds(1),
          deletedAt = null
      )
      val patientProfileTwo = patientProfile.copy(patient = patientTwo, address = addressTwo, businessIds = listOf(businessIdTwo))

      patientProfileOne to patientProfileTwo
    }

    val patientWithDeletedBusinessId = patientProfileTemplate.let { patientProfile ->
      val patientUuid = UUID.randomUUID()
      val addressUuid = UUID.randomUUID()

      val patient = patientProfile.patient.copy(
          uuid = patientUuid,
          fullName = "Patient with deleted business ID",
          addressUuid = addressUuid
      )
      val address = patientProfile.address.copy(uuid = addressUuid)
      val businessId = BusinessId(
          uuid = UUID.randomUUID(),
          patientUuid = patientUuid,
          identifier = Identifier(deletedBusinessIdentifier, identifierType),
          metaDataVersion = metaVersion,
          metaData = "",
          createdAt = now,
          updatedAt = now,
          deletedAt = now
      )

      patientProfile.copy(patient = patient, address = address, businessIds = listOf(businessId))
    }


    patientRepository.save(listOf(
        patientWithUniqueBusinessId,
        patientOneWithSharedBusinessId,
        patientTwoWithSharedBusinessId,
        patientWithDeletedBusinessId)
    ).blockingAwait()

    val (patientResultOne) = patientRepository.findPatientWithBusinessId(identifier = uniqueBusinessIdentifier).blockingFirst() as Just<Patient>
    assertThat(patientResultOne).isEqualTo(patientWithUniqueBusinessId.patient)

    val (patientResultTwo) = patientRepository.findPatientWithBusinessId(identifier = sharedBusinessIdentifier).blockingFirst() as Just<Patient>
    assertThat(patientResultTwo).isEqualTo(patientTwoWithSharedBusinessId.patient)

    assertThat(patientRepository.findPatientWithBusinessId(deletedBusinessIdentifier).blockingFirst()).isEqualTo(None)
    assertThat(patientRepository.findPatientWithBusinessId("missing_identifier").blockingFirst()).isEqualTo(None)
  }

  @Test
  fun checking_for_whether_a_patient_is_a_defaulter_should_work_as_expected() {

    fun savePatientRecord(
        fullName: String,
        bpMeasurement: List<BloodPressureMeasurement>?,
        hasHadHeartAttack: MedicalHistory.Answer = NO,
        hasHadStroke: MedicalHistory.Answer = NO,
        hasDiabetes: MedicalHistory.Answer = NO,
        hasHadKidneyDisease: MedicalHistory.Answer = NO,
        protocolDrug: ProtocolDrug?,
        appointmentDate: LocalDate?
    ): Pair<UUID, String> {
      val patientUuid = patientRepository.saveOngoingEntry(testData.ongoingPatientEntry(fullName = fullName))
          .andThen(patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility))
          .blockingGet()
          .uuid

      bpMeasurement?.forEach {
        bloodPressureRepository.save(listOf(testData.bloodPressureMeasurement(
            patientUuid = patientUuid,
            systolic = it.systolic,
            diastolic = it.diastolic,
            recordedAt = it.recordedAt
        ))).blockingAwait()
      }
      medicalHistoryRepository.save(listOf(testData.medicalHistory(
          patientUuid = patientUuid,
          hasDiabetes = hasDiabetes,
          hasHadHeartAttack = hasHadHeartAttack,
          hasHadKidneyDisease = hasHadKidneyDisease,
          hasHadStroke = hasHadStroke))).blockingAwait()

      protocolDrug?.let {
        prescriptionRepository.savePrescription(patientUuid = patientUuid, drug = it, facility = currentFacility).blockingAwait()
      }

      appointmentDate?.let {
        appointmentRepository.schedule(
            patientUuid = patientUuid,
            appointmentDate = it,
            appointmentType = Manual,
            currentFacility = testData.qaFacility()
        ).blockingGet()
      }

      return patientUuid to fullName
    }

    val patients = mutableListOf<Pair<UUID, String>>()

    patients += savePatientRecord(
        fullName = "Systolic > 140",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 170,
            diastolic = 80
        )),
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Diastolic > 90",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 130,
            diastolic = 100
        )),
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Has Diabetes",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 120,
            diastolic = 70
        )),
        hasDiabetes = YES,
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Has had stroke",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 120,
            diastolic = 70
        )),
        hasHadStroke = YES,
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Has kidney disease",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 120,
            diastolic = 70
        )),
        hasHadKidneyDisease = YES,
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Had heart attack",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 120,
            diastolic = 70
        )),
        hasHadHeartAttack = YES,
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Drugs prescribed",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 120,
            diastolic = 70
        )),
        protocolDrug = testData.protocolDrug(),
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Appointment already scheduled",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 180,
            diastolic = 70
        )),
        protocolDrug = null,
        appointmentDate = LocalDate.now(testClock).plusDays(10))

    patients += savePatientRecord(
        fullName = "BP deleted, Has had heart attack",
        bpMeasurement = listOf(testData.bloodPressureMeasurement(
            systolic = 180,
            diastolic = 70,
            deletedAt = Instant.now(testClock)
        )),
        hasHadHeartAttack = YES,
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Multiple BPs",
        bpMeasurement = listOf(
            testData.bloodPressureMeasurement(
                systolic = 180,
                diastolic = 70,
                recordedAt = Instant.now(testClock).minus(40, ChronoUnit.DAYS)),
            testData.bloodPressureMeasurement(
                systolic = 180,
                diastolic = 70,
                recordedAt = Instant.now(testClock).minus(10, ChronoUnit.DAYS)
            )),
        protocolDrug = null,
        appointmentDate = null)

    patients += savePatientRecord(
        fullName = "Last recorded BP is normal",
        bpMeasurement = listOf(
            testData.bloodPressureMeasurement(
                systolic = 180,
                diastolic = 70,
                recordedAt = Instant.now(testClock).minus(40, ChronoUnit.DAYS)),
            testData.bloodPressureMeasurement(
                systolic = 120,
                diastolic = 70,
                recordedAt = Instant.now(testClock).minus(10, ChronoUnit.DAYS)
            )),
        protocolDrug = null,
        appointmentDate = null)

    val defaulterPatients = mutableListOf<String>()

    patients.map { (uuid, name) ->
      val isDefaulter = patientRepository.isPatientDefaulter(uuid).blockingFirst()
      if (isDefaulter) {
        defaulterPatients += name
      }
    }

    assertThat(defaulterPatients).containsExactlyElementsIn(mutableListOf(
        "Systolic > 140",
        "Diastolic > 90",
        "Has Diabetes",
        "Has had stroke",
        "Has kidney disease",
        "Had heart attack",
        "Drugs prescribed",
        "BP deleted, Has had heart attack",
        "Multiple BPs"))
  }

  @Test
  fun when_fetching_bp_passport_for_patient_then_the_latest_one_should_be_returned() {
    val patientUuid = UUID.randomUUID()
    val identifier = Identifier(patientUuid.toString(), BpPassport)


    val oldBpPassport = testData.businessId(
        patientUuid = patientUuid,
        identifier = identifier,
        createdAt = Instant.now(testClock),
        deletedAt = null
    )

    testClock.advanceBy(Duration.ofMinutes(10))

    val currentBpPassport = testData.businessId(
        patientUuid = patientUuid,
        identifier = identifier,
        createdAt = Instant.now(testClock),
        deletedAt = null
    )
    val deletedBpPassport = testData.businessId(
        patientUuid = patientUuid,
        identifier = identifier,
        createdAt = Instant.now(testClock),
        deletedAt = Instant.now(testClock)
    )

    val dummyProfile = testData.patientProfile(patientUuid = patientUuid, generateBusinessId = false)
    val profileWithBusinessIds = dummyProfile.copy(businessIds = listOf(oldBpPassport, currentBpPassport, deletedBpPassport))

    patientRepository.save(listOf(profileWithBusinessIds)).blockingAwait()

    val (latestBpPassport) = patientRepository.bpPassportForPatient(patientUuid).blockingFirst()

    assertThat(latestBpPassport).isEqualTo(currentBpPassport)
  }

  @Test
  fun when_updating_patient_recordedAt_then_it_should_compare_and_then_update_the_date() {

    fun createPatientProfile(patientUuid: UUID, recordedAt: Instant): PatientProfile {
      return testData.patientProfile(patientUuid = patientUuid)
          .run {
            copy(patient = patient.copy(
                recordedAt = recordedAt,
                updatedAt = Instant.now(testClock),
                syncStatus = DONE)
            )
          }
    }

    val patientUuid1 = UUID.randomUUID()
    val recordedAtForPatient1 = Instant.now(testClock)
    val patientProfile1 = createPatientProfile(patientUuid1, recordedAtForPatient1)
    testClock.advanceBy(Duration.ofMinutes(1))
    val instantToCompare1 = Instant.now(testClock)

    val patientUuid2 = UUID.randomUUID()
    val instantToCompare2 = Instant.now(testClock)
    testClock.advanceBy(Duration.ofMinutes(1))
    val recordedAtForPatient2 = Instant.now(testClock)
    val patientProfile2 = createPatientProfile(patientUuid2, recordedAtForPatient2)

    patientRepository.save(listOf(patientProfile1, patientProfile2)).blockingAwait()

    patientRepository.compareAndUpdateRecordedAt(patientUuid1, instantToCompare1).blockingAwait()
    patientRepository.compareAndUpdateRecordedAt(patientUuid2, instantToCompare2).blockingAwait()

    val patient1 = patientRepository.patient(patientUuid1).blockingFirst().toNullable()!!
    val patient2 = patientRepository.patient(patientUuid2).blockingFirst().toNullable()!!

    assertThat(patient1.recordedAt).isEqualTo(recordedAtForPatient1)
    assertThat(patient1.syncStatus).isEqualTo(DONE)

    assertThat(patient2.recordedAt).isEqualTo(instantToCompare2)
    assertThat(patient2.syncStatus).isEqualTo(PENDING)
    assertThat(patient2.updatedAt).isEqualTo(Instant.now(testClock))
  }

  @Test
  fun when_patient_recorded_at_needs_to_be_updated_then_it_should_be_set_based_on_oldest_BP() {
    fun loadPatient(patientUuid: UUID) = patientRepository.patient(patientUuid).blockingFirst().toNullable()!!

    val patientUuid = UUID.randomUUID()
    val patientCreatedDate = Instant.now(clock)
    val patient = testData.patientProfile(patientUuid = patientUuid)
        .run {
          copy(patient = patient.copy(
              createdAt = patientCreatedDate,
              recordedAt = patientCreatedDate,
              updatedAt = Instant.now(testClock),
              syncStatus = DONE)
          )
        }
    patientRepository.save(listOf(patient)).blockingAwait()

    val bpRecordedTwoDaysBeforePatientCreated = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        deletedAt = null,
        recordedAt = patientCreatedDate.minus(2, ChronoUnit.DAYS)
    )
    val bpRecordedOneDayBeforePatientCreated = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        deletedAt = null,
        recordedAt = patientCreatedDate.minus(1, ChronoUnit.DAYS)
    )
    val bpRecordedOneMinuteAfterPatientCreated = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        deletedAt = null,
        recordedAt = patientCreatedDate.plus(1, ChronoUnit.MINUTES)
    )

    bloodPressureRepository.save(listOf(
        bpRecordedTwoDaysBeforePatientCreated,
        bpRecordedOneDayBeforePatientCreated,
        bpRecordedOneMinuteAfterPatientCreated
    )).blockingAwait()
    patientRepository.updateRecordedAt(patientUuid).blockingAwait()
    loadPatient(patientUuid).let { savedPatient ->
      val expectedRecordedAt = bpRecordedTwoDaysBeforePatientCreated.recordedAt

      assertThat(savedPatient.recordedAt).isEqualTo(expectedRecordedAt)
      assertThat(savedPatient.syncStatus).isEqualTo(PENDING)
      assertThat(savedPatient.updatedAt).isEqualTo(Instant.now(clock))
    }

    bloodPressureRepository.markBloodPressureAsDeleted(bpRecordedTwoDaysBeforePatientCreated).blockingAwait()
    patientRepository.updateRecordedAt(patientUuid).blockingAwait()
    loadPatient(patientUuid).let { savedPatient ->
      val expectedRecordedAt = bpRecordedOneDayBeforePatientCreated.recordedAt

      assertThat(savedPatient.recordedAt).isEqualTo(expectedRecordedAt)
      assertThat(savedPatient.syncStatus).isEqualTo(PENDING)
      assertThat(savedPatient.updatedAt).isEqualTo(Instant.now(clock))
    }

    bloodPressureRepository.markBloodPressureAsDeleted(bpRecordedOneDayBeforePatientCreated).blockingAwait()
    patientRepository.updateRecordedAt(patientUuid).blockingAwait()
    loadPatient(patientUuid).let { savedPatient ->
      assertThat(savedPatient.recordedAt).isEqualTo(patientCreatedDate)
      assertThat(savedPatient.syncStatus).isEqualTo(PENDING)
      assertThat(savedPatient.updatedAt).isEqualTo(Instant.now(clock))
    }

    bloodPressureRepository.markBloodPressureAsDeleted(bpRecordedOneMinuteAfterPatientCreated).blockingAwait()
    patientRepository.updateRecordedAt(patientUuid).blockingAwait()
    loadPatient(patientUuid).let { savedPatient ->
      assertThat(savedPatient.recordedAt).isEqualTo(patientCreatedDate)
      assertThat(savedPatient.syncStatus).isEqualTo(PENDING)
      assertThat(savedPatient.updatedAt).isEqualTo(Instant.now(clock))
    }
  }

  @Test
  fun querying_whether_patient_has_changed_should_work_as_expected() {
    fun hasPatientChangedSince(patientUuid: UUID, since: Instant): Boolean {
      return patientRepository.hasPatientChangedSince(patientUuid, since).blockingFirst()
    }

    val patientUpdatedAt = Instant.now(clock)
    val patientProfile = testData.patientProfile(syncStatus = PENDING).let { patientProfile ->
      patientProfile.copy(patient = patientProfile.patient.copy(updatedAt = patientUpdatedAt))
    }
    val patientUuid = patientProfile.patient.uuid

    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val oneSecondAfterPatientUpdated = patientUpdatedAt.plus(Duration.ofSeconds(1L))
    val oneSecondBeforePatientUpdated = patientUpdatedAt.minus(Duration.ofSeconds(1L))

    assertThat(hasPatientChangedSince(patientUuid, oneSecondBeforePatientUpdated)).isTrue()
    assertThat(hasPatientChangedSince(patientUuid, patientUpdatedAt)).isFalse()
    assertThat(hasPatientChangedSince(patientUuid, oneSecondAfterPatientUpdated)).isFalse()

    patientRepository.setSyncStatus(listOf(patientUuid), DONE).blockingAwait()

    assertThat(hasPatientChangedSince(patientUuid, patientUpdatedAt)).isFalse()
    assertThat(hasPatientChangedSince(patientUuid, oneSecondAfterPatientUpdated)).isFalse()
    assertThat(hasPatientChangedSince(patientUuid, oneSecondBeforePatientUpdated)).isFalse()
  }

  @Test
  fun patient_edits_should_not_affect_recent_patients() {
    val patient1Uuid = UUID.randomUUID()
    val patient2Uuid = UUID.randomUUID()
    val patient1AddressUuid = UUID.randomUUID()

    val recentPatient1 = savePatientWithBp(patientUuid = patient1Uuid, patientAddressUuid = patient1AddressUuid)
    val recentPatient2 = savePatientWithBp(patientUuid = patient2Uuid)

    verifyRecentPatientOrder(recentPatient2, recentPatient1)

    patientRepository
        .updatePatient(testData.patient(
            uuid = patient1Uuid,
            fullName = "new name",
            addressUuid = patient1AddressUuid,
            age = recentPatient1.age,
            gender = recentPatient1.gender,
            status = ACTIVE
        ))
        .blockingAwait()

    verifyRecentPatientOrder(recentPatient2, recentPatient1.copy(fullName = "new name"))
  }

  @Test
  fun patients_that_are_not_active_should_not_come_up_in_recent_patients_list() {
    val facilityUuid = UUID.randomUUID()

    val patient1 = savePatientWithBp(facilityUuid = facilityUuid, patientStatus = ACTIVE)
    verifyRecentPatientOrder(patient1, facilityUuid = facilityUuid)

    val patient2 = savePatientWithBp(facilityUuid = facilityUuid, patientStatus = ACTIVE)
    verifyRecentPatientOrder(patient2, patient1, facilityUuid = facilityUuid)

    savePatientWithBp(facilityUuid = facilityUuid, patientStatus = DEAD)
    verifyRecentPatientOrder(patient2, patient1, facilityUuid = facilityUuid)

    savePatientWithBp(facilityUuid = facilityUuid, patientStatus = MIGRATED)
    verifyRecentPatientOrder(patient2, patient1, facilityUuid = facilityUuid)

    savePatientWithBp(facilityUuid = facilityUuid, patientStatus = UNRESPONSIVE)
    verifyRecentPatientOrder(patient2, patient1, facilityUuid = facilityUuid)

    savePatientWithBp(facilityUuid = facilityUuid, patientStatus = INACTIVE)
    verifyRecentPatientOrder(patient2, patient1, facilityUuid = facilityUuid)
  }

  @Test
  fun querying_all_patients_in_a_facility_should_return_only_patients_in_that_facility_ordered_by_name() {

    fun recordPatientAtFacility(
        patientName: String,
        status: PatientStatus,
        facility: Facility
    ) {
      val patientUuid = UUID.randomUUID()
      val patientProfile = testData.patientProfile(patientUuid = patientUuid).let { patientProfile ->
        patientProfile.copy(patient = patientProfile.patient.copy(status = status, fullName = patientName))
      }
      val bp = testData.bloodPressureMeasurement(patientUuid = patientUuid, facilityUuid = facility.uuid)

      patientRepository.save(listOf(patientProfile)).blockingAwait()
      bloodPressureRepository.save(listOf(bp)).blockingAwait()
    }

    // given
    val (facilityA, facilityB, facilityC) = facilityRepository.facilities().blockingFirst()

    recordPatientAtFacility(
        "Chitra",
        ACTIVE,
        facilityA
    )
    recordPatientAtFacility(
        "Anubhav",
        ACTIVE,
        facilityA
    )
    recordPatientAtFacility(
        "Bhim",
        DEAD,
        facilityA
    )
    recordPatientAtFacility(
        "Elvis",
        DEAD,
        facilityB
    )
    recordPatientAtFacility(
        "Farhan",
        DEAD,
        facilityB
    )
    recordPatientAtFacility(
        "Dhruv",
        ACTIVE,
        facilityB
    )

    // when
    fun patientsInFacility(facility: Facility): List<PatientSearchResult> {
      return patientRepository
          .allPatientsInFacility(facility)
          .blockingFirst()
    }

    val allPatientFullNamesInFacilityA = patientsInFacility(facilityA).map { it.fullName }
    val allPatientFullNamesInFacilityB = patientsInFacility(facilityB).map { it.fullName }
    val allPatientFullNamesInFacilityC = patientsInFacility(facilityC).map { it.fullName }

    // then
    assertThat(allPatientFullNamesInFacilityA)
        .containsExactly("Anubhav", "Chitra")
        .inOrder()

    assertThat(allPatientFullNamesInFacilityB)
        .containsExactly("Dhruv")
        .inOrder()

    assertThat(allPatientFullNamesInFacilityC).isEmpty()
  }

  @Test
  fun querying_all_patients_in_a_facility_should_set_the_last_bp_to_the_last_recorded_one_within_any_facility() {

    fun createPatient(patientUuid: UUID, patientName: String): PatientProfile {
      val patientProfile = testData.patientProfile(patientUuid = patientUuid).let { patientProfile ->
        patientProfile.copy(patient = patientProfile.patient.copy(status = ACTIVE, fullName = patientName))
      }
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      return patientProfile
    }

    fun recordBp(uuid: UUID, patientUuid: UUID, facilityUuid: UUID, recordedAt: Instant) {
      val bp = testData.bloodPressureMeasurement(uuid = uuid, patientUuid = patientUuid, facilityUuid = facilityUuid, recordedAt = recordedAt)

      bloodPressureRepository.save(listOf(bp)).blockingAwait()
    }

    // given
    val (facilityA, facilityB) = facilityRepository.facilities().blockingFirst()
    val now = Instant.parse("2019-06-26T00:00:00Z")
    val oneSecondLater = now.plusSeconds(1)
    val oneSecondEarlier = now.minusSeconds(1)
    val fiveSecondsLater = now.plusSeconds(5)

    val uuidOfPatientA = UUID.fromString("e2a49529-3b46-4c9e-a7bc-af3090f1ecbb")
    val uuidOfBp1OfPatientA = UUID.fromString("588b0d8c-6dce-4664-863f-8c79c06c6c3c")
    val uuidOfBp2OfPatientA = UUID.fromString("ba875b98-4ca0-4097-bbc7-56041b377cf2")
    val uuidOfBp3OfPatientA = UUID.fromString("4b92601e-4a86-40de-b6b7-707f9122a81b")
    createPatient(patientUuid = uuidOfPatientA, patientName = "Patient with latest BP in Facility A")
    recordBp(uuid = uuidOfBp1OfPatientA, patientUuid = uuidOfPatientA, facilityUuid = facilityA.uuid, recordedAt = oneSecondLater)
    recordBp(uuid = uuidOfBp2OfPatientA, patientUuid = uuidOfPatientA, facilityUuid = facilityB.uuid, recordedAt = oneSecondEarlier)
    recordBp(uuid = uuidOfBp3OfPatientA, patientUuid = uuidOfPatientA, facilityUuid = facilityA.uuid, recordedAt = now)

    val uuidOfPatientB = UUID.fromString("7925e13f-3b04-46b0-b685-7005ebb1b6fd")
    val uuidOfBp1OfPatientB = UUID.fromString("00e39456-f5e4-4538-956d-7e973ec5da88")
    val uuidOfBp2OfPatientB = UUID.fromString("b72e28cd-1b46-4c72-84cd-58530c2829e3")
    val uuidOfBp3OfPatientB = UUID.fromString("c18b41d5-260e-4a26-8030-45c621ded98d")
    createPatient(patientUuid = uuidOfPatientB, patientName = "Patient with latest BP in Facility B")
    recordBp(uuid = uuidOfBp1OfPatientB, patientUuid = uuidOfPatientB, facilityUuid = facilityB.uuid, recordedAt = fiveSecondsLater)
    recordBp(uuid = uuidOfBp2OfPatientB, patientUuid = uuidOfPatientB, facilityUuid = facilityA.uuid, recordedAt = oneSecondEarlier)
    recordBp(uuid = uuidOfBp3OfPatientB, patientUuid = uuidOfPatientB, facilityUuid = facilityB.uuid, recordedAt = now)

    // when
    fun patientsInFacility(facility: Facility): List<PatientSearchResult> {
      return patientRepository
          .allPatientsInFacility(facility)
          .blockingFirst()
    }

    data class PatientUuidAndLatestBpRecorded(val patientUuid: UUID, val latestBpRecordedAt: Instant) {
      constructor(patientSearchResult: PatientSearchResult) : this(patientSearchResult.uuid, patientSearchResult.lastBp!!.takenOn)
    }

    val patientsAndLatestBpRecordedAtFacilityA = patientsInFacility(facilityA)
        .map(::PatientUuidAndLatestBpRecorded)
    val patientsAndLatestBpRecordedAtFacilityB = patientsInFacility(facilityB)
        .map(::PatientUuidAndLatestBpRecorded)

    // then
    assertThat(patientsAndLatestBpRecordedAtFacilityA)
        .containsExactly(
            PatientUuidAndLatestBpRecorded(patientUuid = uuidOfPatientA, latestBpRecordedAt = oneSecondLater),
            PatientUuidAndLatestBpRecorded(patientUuid = uuidOfPatientB, latestBpRecordedAt = fiveSecondsLater)
        )
        .inOrder()

    assertThat(patientsAndLatestBpRecordedAtFacilityB)
        .containsExactly(
            PatientUuidAndLatestBpRecorded(patientUuid = uuidOfPatientA, latestBpRecordedAt = oneSecondLater),
            PatientUuidAndLatestBpRecorded(patientUuid = uuidOfPatientB, latestBpRecordedAt = fiveSecondsLater)
        )
        .inOrder()
  }
}
