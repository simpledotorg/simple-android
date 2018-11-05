package org.simple.clinic.patient

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.communication.CommunicationRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.TestClock
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
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
  lateinit var database: AppDatabase

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var bpRepository: BloodPressureRepository

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var clock: Clock

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    (clock as TestClock).setYear(2018)
  }

  @Test
  fun when_a_patient_with_phone_numbers_is_saved_then_it_should_be_correctly_stored_in_the_database() {
    val ongoingAddress = OngoingNewPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails("Ashok Kumar", "08/04/1985", null, Gender.TRANSGENDER)
    val ongoingPhoneNumber = OngoingNewPatientEntry.PhoneNumber(number = "227788", type = PatientPhoneNumberType.LANDLINE)

    val personalDetailsOnlyEntry = OngoingNewPatientEntry(personalDetails = ongoingPersonalDetails)

    patientRepository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(patientRepository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
        .flatMapCompletable { withAddressAndPhoneNumbers -> patientRepository.saveOngoingEntry(withAddressAndPhoneNumbers) }
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = patientRepository.search("lakshman", includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = patientRepository.search("ashok", includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search2).hasSize(1)
    assertThat(search2.first().age).isNull()
    assertThat(search2.first().dateOfBirth).isEqualTo(LocalDate.parse("1985-04-08"))
    assertThat(search2.first().phoneNumber).isNotEmpty()
    assertThat(search2.first().phoneNumber).isEqualTo("227788")
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
          .andThen(patientRepository.saveOngoingEntryAsPatient())
          .blockingGet()

      assertThat(patient.searchableName).isEqualTo(expectedSearchableName)
    }
  }

  @Test
  fun when_a_patient_without_phone_numbers_is_saved_then_it_should_be_correctly_stored_in_the_database() {
    val patientEntry = testData.ongoingPatientEntry(fullName = "Jeevan Bima", phone = null)

    patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = patientRepository.search("lakshman").blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = patientRepository.search("bima").blockingFirst()
    assertThat(search2).hasSize(1)
    assertThat(search2.first().phoneNumber).isNull()
  }

  @Test
  fun when_a_patient_with_null_dateofbirth_and_nonnull_age_is_saved_then_it_should_be_correctly_stored_in_the_database() {
    val patientEntry = testData.ongoingPatientEntry(fullName = "Ashok Kumar")

    patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = patientRepository.search("lakshman", includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = patientRepository.search("ashok", includeFuzzyNameSearch = false).blockingFirst()
    val patient = search2[0]
    assertThat(patient.fullName).isEqualTo(patientEntry.personalDetails!!.fullName)
    assertThat(patient.dateOfBirth).isNull()
    assertThat(patient.age!!.value).isEqualTo(patientEntry.personalDetails!!.age!!.toInt())
    assertThat(patient.phoneNumber!!).isEqualTo(patientEntry.phoneNumber!!.number)
  }

  @Test
  fun when_a_patient_with_null_dateofbirth_and_null_age_is_saved_then_it_should_not_be_accepted() {
    val patientEntry = testData.ongoingPatientEntry(dateOfBirth = null, age = null)

    patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .test()
        .assertError(AssertionError::class.java)
  }

  @Test
  fun patient_search_should_ignore_spaces_and_whitespace_characters() {
    val names = arrayOf("Riya Puri", "Manabi    Mehra", "Amit:Sodhi")
    val searches = arrayOf("ya p" to true, "bime" to true, "ito" to false)

    names.forEachIndexed { index, fullName ->
      val patientEntry = testData.ongoingPatientEntry(fullName = fullName)

      patientRepository.saveOngoingEntry(patientEntry)
          .andThen(patientRepository.saveOngoingEntryAsPatient())
          .blockingGet()

      val (query, shouldFindInDb) = searches[index]
      val search = patientRepository.search(query, includeFuzzyNameSearch = false).blockingFirst()

      if (shouldFindInDb) {
        assertThat(search).hasSize(1)
        assertThat(search.first().fullName).isEqualTo(fullName)
      } else {
        assertThat(search).isEmpty()
      }
    }
  }

  @Test
  fun when_a_patient_with_address_is_saved_then_search_should_correctly_return_combined_object() {
    val patientEntry = testData.ongoingPatientEntry(fullName = "Asha Kumar", dateOfBirth = "15/08/1947", age = null)

    patientRepository.saveOngoingEntry(patientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val combinedPatient = patientRepository.search("kumar")
        .blockingFirst()
        .first()

    assertThat(combinedPatient.fullName).isEqualTo("Asha Kumar")
    assertThat(combinedPatient.gender).isEqualTo(patientEntry.personalDetails!!.gender)
    assertThat(combinedPatient.dateOfBirth).isEqualTo(LocalDate.parse("1947-08-15"))
    assertThat(combinedPatient.createdAt).isAtLeast(combinedPatient.address.createdAt)
    assertThat(combinedPatient.syncStatus).isEqualTo(SyncStatus.PENDING)
    assertThat(combinedPatient.address.colonyOrVillage).isEqualTo(patientEntry.address!!.colonyOrVillage)
    assertThat(combinedPatient.address.state).isEqualTo(patientEntry.address!!.state)
    assertThat(combinedPatient.phoneNumber).isNotEmpty()
    assertThat(combinedPatient.phoneNumber).isEqualTo(patientEntry.phoneNumber!!.number)
    assertThat(combinedPatient.phoneActive).isEqualTo(patientEntry.phoneNumber!!.active)
  }

  @Test
  fun when_patients_with_date_of_birth_are_present_and_age_filter_is_applied_then_search_should_correctly_find_them() {
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails("Abhay Kumar", "15/08/1950", null, Gender.TRANSGENDER)
    val ongoingAddress = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val ongoingPhoneNumber = OngoingNewPatientEntry.PhoneNumber("3914159", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPatientEntry = OngoingNewPatientEntry(ongoingPersonalDetails, ongoingAddress, ongoingPhoneNumber)
    patientRepository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd2 = OngoingNewPatientEntry.PersonalDetails("Alok Kumar", "15/08/1940", null, Gender.TRANSGENDER)
    val opa2 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn2 = OngoingNewPatientEntry.PhoneNumber("3418959", PatientPhoneNumberType.MOBILE, active = true)
    val ope2 = OngoingNewPatientEntry(opd2, opa2, opn2)
    patientRepository.saveOngoingEntry(ope2)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd3 = OngoingNewPatientEntry.PersonalDetails("Abhishek Kumar", "1/01/1949", null, Gender.TRANSGENDER)
    val opa3 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn3 = OngoingNewPatientEntry.PhoneNumber("9989159", PatientPhoneNumberType.MOBILE, active = true)
    val ope3 = OngoingNewPatientEntry(opd3, opa3, opn3)
    patientRepository.saveOngoingEntry(ope3)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd4 = OngoingNewPatientEntry.PersonalDetails("Abshot Kumar", "12/10/1951", null, Gender.TRANSGENDER)
    val opa4 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn4 = OngoingNewPatientEntry.PhoneNumber("1991591", PatientPhoneNumberType.MOBILE, active = true)
    val ope4 = OngoingNewPatientEntry(opd4, opa4, opn4)
    patientRepository.saveOngoingEntry(ope4)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val search0 = patientRepository.search("kumar", 12, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search0).hasSize(0)

    val search1 = patientRepository.search("kumar", 77, includeFuzzyNameSearch = false).blockingFirst()
    val person1 = search1.first()
    assertThat(search1).hasSize(1)
    assertThat(person1.fullName).isEqualTo("Alok Kumar")
    assertThat(person1.dateOfBirth).isEqualTo(LocalDate.parse("1940-08-15"))
    assertThat(person1.phoneNumber).isEqualTo("3418959")

    val search2 = patientRepository.search("ab", 68, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search2).hasSize(3)
    assertThat(search2[0].fullName).isEqualTo("Abhay Kumar")
    assertThat(search2[0].dateOfBirth).isEqualTo(LocalDate.parse("1950-08-15"))
    assertThat(search2[1].fullName).isEqualTo("Abhishek Kumar")
    assertThat(search2[1].dateOfBirth).isEqualTo(LocalDate.parse("1949-01-01"))
    assertThat(search2[2].fullName).isEqualTo("Abshot Kumar")
    assertThat(search2[2].dateOfBirth).isEqualTo(LocalDate.parse("1951-10-12"))
  }

  @Test
  fun when_patients_with_age_are_present_and_age_filter_is_applied_then_search_should_correctly_find_them() {
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails("Abhay Kumar", null, "20", Gender.TRANSGENDER)
    val ongoingAddress = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val ongoingPhoneNumber = OngoingNewPatientEntry.PhoneNumber("34314159", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPatientEntry = OngoingNewPatientEntry(ongoingPersonalDetails, ongoingAddress, ongoingPhoneNumber)
    patientRepository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd2 = OngoingNewPatientEntry.PersonalDetails("Alok Kumar", null, "17", Gender.FEMALE)
    val opa2 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn2 = OngoingNewPatientEntry.PhoneNumber("3413459", PatientPhoneNumberType.MOBILE, active = true)
    val ope2 = OngoingNewPatientEntry(opd2, opa2, opn2)
    patientRepository.saveOngoingEntry(ope2)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd3 = OngoingNewPatientEntry.PersonalDetails("Abhishek Kumar", null, "26", Gender.FEMALE)
    val opa3 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn3 = OngoingNewPatientEntry.PhoneNumber("9913459", PatientPhoneNumberType.MOBILE, active = true)
    val ope3 = OngoingNewPatientEntry(opd3, opa3, opn3)
    patientRepository.saveOngoingEntry(ope3)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd4 = OngoingNewPatientEntry.PersonalDetails("Abshot Kumar", null, "19", Gender.FEMALE)
    val opa4 = OngoingNewPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn4 = OngoingNewPatientEntry.PhoneNumber("1991591", PatientPhoneNumberType.MOBILE, active = true)
    val ope4 = OngoingNewPatientEntry(opd4, opa4, opn4)
    patientRepository.saveOngoingEntry(ope4)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val search0 = patientRepository.search("kumar", 50, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search0).hasSize(0)

    val search1 = patientRepository.search("kumar", 28, includeFuzzyNameSearch = false).blockingFirst()
    val person1 = search1.first()
    assertThat(search1).hasSize(1)
    assertThat(person1.fullName).isEqualTo("Abhishek Kumar")
    assertThat(person1.age!!.value).isEqualTo(26)
    assertThat(person1.phoneNumber).isEqualTo("9913459")

    val search2 = patientRepository.search("ab", 18, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search2).hasSize(2)
    assertThat(search2[0].fullName).isEqualTo("Abhay Kumar")
    assertThat(search2[0].age!!.value).isEqualTo(20)
    assertThat(search2[1].fullName).isEqualTo("Abshot Kumar")
    assertThat(search2[1].age!!.value).isEqualTo(19)
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
    val bloodPressurePayloads = rangeOfRecords.map { testData.bpPayload(patientUuid = patientUuid, facilytyUuid = facilityUuid) }
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

    // We need to ensure that ONLY the tables related to the patient get cleared,
    // and the ones referring to the user must be left untouched

    assertThat(database.patientDao().patientCount().blockingFirst()).isGreaterThan(0)
    assertThat(database.addressDao().count()).isGreaterThan(0)
    assertThat(database.phoneNumberDao().count()).isGreaterThan(0)
    assertThat(database.bloodPressureDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.prescriptionDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.facilityDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.userDao().userImmediate()).isNotNull()
    assertThat(database.userFacilityMappingDao().mappingsForUser(user.uuid).blockingFirst()).isNotEmpty()
    assertThat(database.appointmentDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.communicationDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.medicalHistoryDao().count().blockingFirst()).isGreaterThan(0)

    patientRepository.clearPatientData().blockingAwait()

    assertThat(database.patientDao().patientCount().blockingFirst()).isEqualTo(0)
    assertThat(database.addressDao().count()).isEqualTo(0)
    assertThat(database.phoneNumberDao().count()).isEqualTo(0)
    assertThat(database.bloodPressureDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.prescriptionDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.appointmentDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.communicationDao().count().blockingFirst()).isEqualTo(0)
    assertThat(database.medicalHistoryDao().count().blockingFirst()).isEqualTo(0)

    assertThat(database.facilityDao().count().blockingFirst()).isGreaterThan(0)
    assertThat(database.userDao().userImmediate()).isNotNull()
    assertThat(database.userFacilityMappingDao().mappingsForUser(user.uuid).blockingFirst()).isNotEmpty()
  }

  @Test
  fun when_searching_with_age_then_patients_whose_last_visited_facility_matches_with_the_current_facility_should_be_present_at_the_top() {
    val user = userSession.requireLoggedInUser().blockingFirst()

    val facilities = facilityRepository.facilities().blockingFirst()
    val currentFacility = facilityRepository.currentFacility(user).blockingFirst()
    val otherFacility = facilities.first { it != currentFacility }

    facilityRepository.associateUserWithFacilities(user, facilities.map { it.uuid }).blockingAwait()

    val data = listOf(
        "Ashoka" to listOf(otherFacility),
        "Ashok Kumari" to listOf(currentFacility),
        "Kumar Ashok" to listOf(currentFacility, currentFacility),
        "Ashoka Kumar" to listOf(otherFacility, currentFacility),
        "Ash Kumari" to listOf())

    Observable.fromIterable(data)
        .flatMapCompletable { (patientName, visitedFacilities) ->
          patientRepository
              .saveOngoingEntry(testData.ongoingPatientEntry(fullName = patientName, age = "20"))
              .andThen(patientRepository.saveOngoingEntryAsPatient())
              .flatMapCompletable { savedPatient ->
                // Record BPs in different facilities.
                Observable.fromIterable(visitedFacilities)
                    .flatMapSingle { facility ->
                      facilityRepository
                          .setCurrentFacility(user, facility)
                          .andThen(bpRepository
                              .saveMeasurement(savedPatient.uuid, systolic = 120, diastolic = 121)
                          )
                    }
                    .ignoreElements()
              }
        }
        .blockingAwait()

    facilityRepository.setCurrentFacility(user, currentFacility).blockingAwait()

    val runAssertions = { searchResults: List<PatientSearchResult> ->
      assertThat(searchResults).hasSize(5)
      assertThat(searchResults[0].fullName).isEqualTo("Ashok Kumari")
      assertThat(searchResults[1].fullName).isEqualTo("Kumar Ashok")
      assertThat(searchResults[2].fullName).isEqualTo("Ashoka Kumar")
      assertThat(searchResults[3].fullName).isEqualTo("Ashoka")
      assertThat(searchResults[4].fullName).isEqualTo("Ash Kumari")
    }

    val resultsWithoutAgeFilter = patientRepository.search("ash", includeFuzzyNameSearch = false).blockingFirst()
    runAssertions(resultsWithoutAgeFilter)

    val resultsWithAgeFilter = patientRepository.search("ash", includeFuzzyNameSearch = false, assumedAge = 20).blockingFirst()
    runAssertions(resultsWithAgeFilter)
  }

  @Test
  fun when_patient_is_marked_dead_they_should_not_show_in_search_results() {
    val patient =
        patientRepository.saveOngoingEntry(testData.ongoingPatientEntry("Ashok Kumar"))
            .andThen(patientRepository.saveOngoingEntryAsPatient())
            .blockingGet()

    patientRepository.updatePatientStatusToDead(patient.uuid).blockingAwait()

    val searchResult = patientRepository.search("Ashok").blockingFirst()
    val patientFirst = patientRepository.patient(patient.uuid).blockingFirst()

    assertThat(patientRepository.recordCount().blockingGet()).isEqualTo(1)
    assertThat(patientFirst).isNotNull()
    assertThat(searchResult).isEmpty()
  }

  /**
   * Added to test the case where SQLite's max query param length (999) can be
   * exceeded during fuzzy name search.
   */
  @Test
  fun when_searching_with_fuzzy_search_the_results_must_be_limited() {
    val template = testData.patientProfile(syncStatus = SyncStatus.DONE)

    val patientsToSave = (1..1000).map {
      val addressUuid = UUID.randomUUID()
      val patientUuid = UUID.randomUUID()

      template.copy(
          patient = template.patient.copy(
              uuid = patientUuid,
              addressUuid = addressUuid,
              fullName = "Name",
              searchableName = "Name",
              dateOfBirth = LocalDate.now(clock).minusYears(10),
              status = PatientStatus.ACTIVE
          ),
          address = template.address.copy(uuid = addressUuid),
          phoneNumbers = template.phoneNumbers.map { number -> number.copy(uuid = UUID.randomUUID(), patientUuid = patientUuid) }
      )
    }

    patientRepository.save(patientsToSave).blockingAwait()
    assertThat(patientRepository.recordCount().blockingGet()).isEqualTo(1000)

    assertThat(patientRepository.search(name = "Fame", includeFuzzyNameSearch = true).blockingFirst().size).isEqualTo(100)
    assertThat(patientRepository.search(name = "Fame", assumedAge = 3, includeFuzzyNameSearch = true).blockingFirst().size).isEqualTo(100)
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

    database.patientDao().save(updatedPatient.toDatabaseModel(SyncStatus.DONE))

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
    database.patientDao().save(updatedPatient.toDatabaseModel(SyncStatus.DONE))

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
            syncStatus = SyncStatus.DONE
        ),
        address = addressToSave,
        phoneNumbers = emptyList()
    )

    val patient = patientProfile.patient

    patientRepository.save(listOf(patientProfile))
        .blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestClock).advanceBy(updatedAfter)

    val (oldSavedAddress) = patientRepository.address(patient.addressUuid).firstOrError().blockingGet() as Just<PatientAddress>

    val newAddressToSave = oldSavedAddress.copy(
        colonyOrVillage = "New Colony",
        district = "New District",
        state = "New State"
    )

    patientRepository.updateAddressForPatient(patientUuid = patient.uuid, patientAddress = newAddressToSave).blockingAwait()

    val (updatedPatient) = patientRepository.patient(patient.uuid).blockingFirst() as Just<Patient>

    assertThat(updatedPatient.syncStatus).isEqualTo(SyncStatus.PENDING)

    val (savedAddress) = patientRepository.address(updatedPatient.addressUuid).firstOrError().blockingGet() as Just<PatientAddress>

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
        syncStatus = SyncStatus.DONE,
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
        )
    )

    patientRepository.save(listOf(patientProfile))
        .blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestClock).advanceBy(updatedAfter)

    val newPatientToSave = originalSavedPatient.copy(
        fullName = "New Name",
        gender = Gender.TRANSGENDER,
        age = Age(value = 35, updatedAt = Instant.now(clock), computedDateOfBirth = LocalDate.now(clock)),
        dateOfBirth = LocalDate.now(clock)
    )

    patientRepository.updatePatient(newPatientToSave).blockingAwait()

    val (savedPatient) = patientRepository.patient(newPatientToSave.uuid).firstOrError().blockingGet() as Just<Patient>

    assertThat(savedPatient.syncStatus).isEqualTo(SyncStatus.PENDING)
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
        syncStatus = SyncStatus.DONE,
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
        )
    )

    patientRepository.save(listOf(patientProfile))
        .blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestClock).advanceBy(updatedAfter)

    val phoneNumberToUpdate = patientProfile.phoneNumbers[1].copy(number = "12345678", phoneType = PatientPhoneNumberType.LANDLINE)

    patientRepository.updatePhoneNumberForPatient(originalSavedPatient.uuid, phoneNumberToUpdate).blockingAwait()

    val phoneNumbersSaved = database.phoneNumberDao().phoneNumber(originalSavedPatient.uuid).firstOrError().blockingGet()

    val phoneNumber = phoneNumbersSaved.find { it.uuid == phoneNumberToUpdate.uuid }

    assertThat(phoneNumber).isNotNull()
    assertThat(phoneNumber!!.number).isEqualTo("12345678")
    assertThat(phoneNumber.phoneType).isEqualTo(PatientPhoneNumberType.LANDLINE)
    assertThat(phoneNumber.updatedAt).isEqualTo(patientProfile.phoneNumbers[1].updatedAt.plus(updatedAfter))
    assertThat(phoneNumber.updatedAt).isNotEqualTo(phoneNumber.createdAt)
  }

  @Test
  fun phone_number_should_be_saved_properly() {
    val addressToSave = testData.patientAddress()

    val originalSavedPatient = testData.patient(
        syncStatus = SyncStatus.DONE,
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
            )
        )
    )

    patientRepository.save(listOf(patientProfile))
        .blockingAwait()

    val updatedAfter = Duration.ofDays(1L)
    (clock as TestClock).advanceBy(updatedAfter)

    patientRepository.savePhoneNumberForPatient(
        patientUuid = originalSavedPatient.uuid,
        number = "2222222222",
        phoneNumberType = PatientPhoneNumberType.MOBILE,
        active = true
    ).blockingAwait()

    val phoneNumbersSaved = database.phoneNumberDao().phoneNumber(originalSavedPatient.uuid).firstOrError().blockingGet()

    assertThat(phoneNumbersSaved.size).isEqualTo(2)

    val savedPhoneNumber = phoneNumbersSaved.find { it != patientProfile.phoneNumbers[0] }!!

    assertThat(savedPhoneNumber.active).isTrue()
    assertThat(savedPhoneNumber.createdAt).isEqualTo(Instant.now(clock))
    assertThat(savedPhoneNumber.createdAt).isEqualTo(savedPhoneNumber.updatedAt)
    assertThat(savedPhoneNumber.number).isEqualTo("2222222222")
    assertThat(savedPhoneNumber.phoneType).isEqualTo(PatientPhoneNumberType.MOBILE)

    val (patient) = patientRepository.patient(originalSavedPatient.uuid).firstOrError().blockingGet() as Just<Patient>

    assertThat(patient.syncStatus).isEqualTo(SyncStatus.PENDING)
  }

  @After
  fun tearDown() {
    (clock as TestClock).resetToEpoch()
  }
}
