package org.resolvetosavelives.red.patient

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.AppDatabase
import org.threeten.bp.LocalDate

@RunWith(AndroidJUnit4::class)
class PatientRepositoryAndroidTest {

  lateinit var patientRepository: PatientRepository
  lateinit var database: AppDatabase

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getTargetContext()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    patientRepository = PatientRepository(database)
  }

  @Test
  fun createAnOngoingPatientEntry_withPhoneNumbers_thenSaveItToDatabase() {
    val ongoingAddress = OngoingPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar", "08-04-1985", null, Gender.TRANSGENDER)
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber(number = "2277", type = PatientPhoneNumberType.LANDLINE)

    val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

    patientRepository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(patientRepository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
        .flatMapCompletable { withAddressAndPhoneNumbers -> patientRepository.saveOngoingEntry(withAddressAndPhoneNumbers) }
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = patientRepository.searchPatientsAndPhoneNumbers("lakshman").blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = patientRepository.searchPatientsAndPhoneNumbers("ashok").blockingFirst()
    assertThat(search2).hasSize(1)
    assertThat(search2.first().age).isNull()
    assertThat(search2.first().dateOfBirth).isEqualTo(LocalDate.parse("1985-04-08"))
    assertThat(search2.first().phoneNumber).isNotEmpty()
    assertThat(search2.first().phoneNumber).isEqualTo("2277")
  }

  @Test
  fun createAnOngoingPatientEntry_withPhoneNumbers_thenSaveItToDatabase_AndSearchByPhoneNumber() {
    for (i in 1..5) {
      val ongoingAddress = OngoingPatientEntry.Address("Benson Town", "Bangalore North", "Karnataka")
      val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("$i Chetan Raju", "25-02-200$i", null, Gender.FEMALE)
      val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber(number = "17121988", type = PatientPhoneNumberType.LANDLINE)

      val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

      patientRepository.saveOngoingEntry(personalDetailsOnlyEntry)
          .andThen(patientRepository.ongoingEntry())
          .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
          .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
          .flatMapCompletable { withAddressAndPhoneNumbers -> patientRepository.saveOngoingEntry(withAddressAndPhoneNumbers) }
          .andThen(patientRepository.saveOngoingEntryAsPatient())
          .subscribe()
    }

    val search1 = database.patientSearchDao().search("lakshman").blockingFirst()
    assertThat(search1).isEmpty()

    val search2: List<PatientSearchResult> = database.patientSearchDao().search("raju").blockingFirst()
    assertThat(search2).hasSize(5)
    assertThat(search2.first().fullName).isEqualTo("1 Chetan Raju")
    assertThat(search2.first().phoneNumber).isEqualTo("17121988")
  }

  @Test
  fun createAnOngoingPatientEntry_withNullDateOfBirth_andAgeFilledIn_thenSaveItCompletely() {
    val ongoingAddress = OngoingPatientEntry.Address("Noida", "Gautam Buddha Nagar", "Uttar Pradesh")
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber("299792458", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar", null, "42", Gender.MALE)

    val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

    patientRepository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(patientRepository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
        .flatMapCompletable { withAddressAndPhoneNumbers -> patientRepository.saveOngoingEntry(withAddressAndPhoneNumbers) }
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = patientRepository.searchPatientsAndPhoneNumbers("lakshman").blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = patientRepository.searchPatientsAndPhoneNumbers("ashok").blockingFirst()
    val patient = search2[0]
    assertThat(patient.fullName).isEqualTo("Ashok Kumar")
    assertThat(patient.dateOfBirth).isNull()
    assertThat(patient.age!!.value).isEqualTo(42)
    assertThat(patient.phoneNumber!!).isNotEmpty()
    assertThat(patient.phoneNumber!!).isEqualTo("299792458")
  }

  @Test
  fun createAnOngoingPatientEntry_withNullDateOfBirth_andNullAgeWhenCreated_thenShouldGetError() {
    val ongoingAddress = OngoingPatientEntry.Address("Noida", "Gautam Buddha Nagar", "Uttar Pradesh")
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar", null, null, Gender.MALE)

    val ongoingPatientEntry = OngoingPatientEntry(
        personalDetails = ongoingPersonalDetails,
        address = ongoingAddress)

    patientRepository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .test()
        .assertError(AssertionError::class.java)
  }

  @Test
  fun patientWithAddressSearch_shouldReturn_correctlyCombinedObject() {
    val ongoingAddress = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber("3.14159", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Asha Kumar", "15-08-1947", null, Gender.FEMALE)

    val ongoingPatientEntry = OngoingPatientEntry(
        personalDetails = ongoingPersonalDetails,
        address = ongoingAddress,
        phoneNumber = ongoingPhoneNumber)

    patientRepository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val combinedPatient = patientRepository.searchPatientsAndPhoneNumbers("kumar")
        .blockingFirst()
        .first()

    assertThat(combinedPatient.fullName).isEqualTo("Asha Kumar")
    assertThat(combinedPatient.gender).isEqualTo(Gender.FEMALE)
    assertThat(combinedPatient.dateOfBirth).isEqualTo(LocalDate.parse("1947-08-15"))
    assertThat(combinedPatient.createdAt).isGreaterThan(combinedPatient.address.createdAt)
    assertThat(combinedPatient.syncStatus).isEqualTo(SyncStatus.PENDING)
    assertThat(combinedPatient.address.colonyOrVillage).isEqualTo("Arambol")
    assertThat(combinedPatient.address.state).isEqualTo("Goa")
    assertThat(combinedPatient.phoneNumber).isNotEmpty()
    assertThat(combinedPatient.phoneNumber).isEqualTo("3.14159")
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    database.close()
  }
}
