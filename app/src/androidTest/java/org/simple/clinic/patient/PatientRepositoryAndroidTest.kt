package org.simple.clinic.patient

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.PatientFaker
import org.simple.clinic.TestClinicApp
import org.threeten.bp.LocalDate
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class PatientRepositoryAndroidTest {

  @Inject
  lateinit var repository: PatientRepository

  @Inject
  lateinit var database: AppDatabase

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun createAnOngoingPatientEntry_withPhoneNumbers_thenSaveItToDatabase() {
    val ongoingAddress = OngoingPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar", "08/04/1985", null, Gender.TRANSGENDER)
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber(number = "2277", type = PatientPhoneNumberType.LANDLINE)

    val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

    repository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(repository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
        .flatMapCompletable { withAddressAndPhoneNumbers -> repository.saveOngoingEntry(withAddressAndPhoneNumbers) }
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = repository.searchPatientsAndPhoneNumbers("lakshman").blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = repository.searchPatientsAndPhoneNumbers("ashok").blockingFirst()
    assertThat(search2).hasSize(1)
    assertThat(search2.first().age).isNull()
    assertThat(search2.first().dateOfBirth).isEqualTo(LocalDate.parse("1985-04-08"))
    assertThat(search2.first().phoneNumber).isNotEmpty()
    assertThat(search2.first().phoneNumber).isEqualTo("2277")
  }

  @Test
  fun createAnOngoingPatientEntry_thenSaveItToDatabase_withSearchableName() {
    val ongoingAddress = OngoingPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")
    val names = arrayOf("Riya Puri" to "RiyaPuri", "Manabi    Mehra" to "ManabiMehra", "Amit:Sodhi" to "AmitSodhi", "Riya.Puri" to "RiyaPuri", "Riya,Puri" to "RiyaPuri")

    names.forEach { (fullName, expectedSearchableName) ->
      val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails(fullName, "08/04/1985", null, Gender.TRANSGENDER)

      val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

      val patient = repository.saveOngoingEntry(personalDetailsOnlyEntry)
          .andThen(repository.ongoingEntry())
          .map { it.copy(address = ongoingAddress) }
          .flatMapCompletable { repository.saveOngoingEntry(it) }
          .andThen(repository.saveOngoingEntryAsPatient())
          .blockingGet()

      assertThat(patient.searchableName).isEqualTo(expectedSearchableName)
    }
  }

  @Test
  fun when_saving_an_ongoing_patient_entry_to_the_database_it_should_also_update_the_fuzzy_search_table() {
    val ongoingAddress = OngoingPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")

    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Riya Puri", "08/04/1985", null, Gender.TRANSGENDER)

    val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

    repository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(repository.ongoingEntry())
        .map { it.copy(address = ongoingAddress) }
        .flatMapCompletable { repository.saveOngoingEntry(it) }
        .andThen(repository.saveOngoingEntryAsPatient())
        .blockingGet()

    val savedEntries = database.fuzzyPatientSearchDao().savedEntries().blockingGet()
    assertThat(savedEntries.size).isEqualTo(1)
    assertThat(savedEntries.first().word).isEqualTo("RiyaPuri")
  }

  @Test
  fun createAnOngoingPatientEntry_withoutPhoneNumber_thenSaveItToDatabase() {
    val ongoingAddress = OngoingPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Jeevan Bima", "08/04/1985", null, Gender.TRANSGENDER)

    val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

    repository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(repository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .flatMapCompletable { withAddressAndPhoneNumbers -> repository.saveOngoingEntry(withAddressAndPhoneNumbers) }
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = repository.searchPatientsAndPhoneNumbers("lakshman").blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = repository.searchPatientsAndPhoneNumbers("bima").blockingFirst()
    assertThat(search2).hasSize(1)
    assertThat(search2.first().age).isNull()
    assertThat(search2.first().dateOfBirth).isEqualTo(LocalDate.parse("1985-04-08"))
    assertThat(search2.first().phoneNumber).isNull()
  }

  @Test
  fun createAnOngoingPatientEntry_withPhoneNumbers_thenSaveItToDatabase_AndSearchByPhoneNumber() {
    for (i in 1..5) {
      val ongoingAddress = OngoingPatientEntry.Address("Benson Town", "Bangalore North", "Karnataka")
      val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("$i Chetan Raju", "25/02/200$i", null, Gender.FEMALE)
      val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber(number = "17121988", type = PatientPhoneNumberType.LANDLINE)

      val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

      repository.saveOngoingEntry(personalDetailsOnlyEntry)
          .andThen(repository.ongoingEntry())
          .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
          .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
          .flatMapCompletable { withAddressAndPhoneNumbers -> repository.saveOngoingEntry(withAddressAndPhoneNumbers) }
          .andThen(repository.saveOngoingEntryAsPatient())
          .subscribe()
    }

    val search1 = repository.searchPatientsAndPhoneNumbers("9999").blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = repository.searchPatientsAndPhoneNumbers("1712").blockingFirst()
    assertThat(search2).hasSize(5)
    assertThat(search2.first().phoneNumber).isEqualTo("17121988")
    assertThat(search2.first().phoneType).isEqualTo(PatientPhoneNumberType.LANDLINE)
  }

  @Test
  fun createAnOngoingPatientEntry_withNullDateOfBirth_andAgeFilledIn_thenSaveItCompletely() {
    val ongoingAddress = OngoingPatientEntry.Address("Noida", "Gautam Buddha Nagar", "Uttar Pradesh")
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber("299792458", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar", null, "42", Gender.MALE)

    val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

    repository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(repository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .map { updatedEntry -> updatedEntry.copy(phoneNumber = ongoingPhoneNumber) }
        .flatMapCompletable { withAddressAndPhoneNumbers -> repository.saveOngoingEntry(withAddressAndPhoneNumbers) }
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val search1 = repository.searchPatientsAndPhoneNumbers("lakshman").blockingFirst()
    assertThat(search1).isEmpty()

    val search2 = repository.searchPatientsAndPhoneNumbers("ashok").blockingFirst()
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

    repository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(repository.saveOngoingEntryAsPatient())
        .test()
        .assertError(AssertionError::class.java)
  }

  @Test
  fun patientSearch_shouldIgnore_SpacesAndWhitespaceCharacters() {
    val ongoingAddress = OngoingPatientEntry.Address("HSR Layout", "Bangalore South", "Karnataka")
    val names = arrayOf("Riya Puri", "Manabi    Mehra", "Amit:Sodhi")
    val searches = arrayOf("ya p" to true, "bime" to true, "ito" to false)

    names.forEachIndexed { index, fullName ->
      val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails(fullName, "08/04/1985", null, Gender.TRANSGENDER)

      val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

      repository.saveOngoingEntry(personalDetailsOnlyEntry)
          .andThen(repository.ongoingEntry())
          .map { it.copy(address = ongoingAddress) }
          .flatMapCompletable { repository.saveOngoingEntry(it) }
          .andThen(repository.saveOngoingEntryAsPatient())
          .blockingGet()

      val (query, shouldFindInDb) = searches[index]
      val search = repository.searchPatientsAndPhoneNumbers(query).blockingFirst()
      if (shouldFindInDb) {
        assertThat(search).hasSize(1)
        assertThat(search.first().fullName).isEqualTo(fullName)
      } else {
        assertThat(search).isEmpty()
      }
    }
  }

  @Test
  fun patientWithAddressSearch_shouldReturn_correctlyCombinedObject() {
    val ongoingAddress = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber("3.14159", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Asha Kumar", "15/08/1947", null, Gender.FEMALE)

    val ongoingPatientEntry = OngoingPatientEntry(
        personalDetails = ongoingPersonalDetails,
        address = ongoingAddress,
        phoneNumber = ongoingPhoneNumber)

    repository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val combinedPatient = repository.searchPatientsAndPhoneNumbers("kumar")
        .blockingFirst()
        .first()

    assertThat(combinedPatient.fullName).isEqualTo("Asha Kumar")
    assertThat(combinedPatient.gender).isEqualTo(Gender.FEMALE)
    assertThat(combinedPatient.dateOfBirth).isEqualTo(LocalDate.parse("1947-08-15"))
    assertThat(combinedPatient.createdAt).isAtLeast(combinedPatient.address.createdAt)
    assertThat(combinedPatient.syncStatus).isEqualTo(SyncStatus.PENDING)
    assertThat(combinedPatient.address.colonyOrVillage).isEqualTo("Arambol")
    assertThat(combinedPatient.address.state).isEqualTo("Goa")
    assertThat(combinedPatient.phoneNumber).isNotEmpty()
    assertThat(combinedPatient.phoneNumber).isEqualTo("3.14159")
    assertThat(combinedPatient.phoneActive).isEqualTo(true)
  }

  @Test
  fun patientSearch_forPatientsWithDateOfBirth_withAgeFilter_shouldReturnCorrectResults() {
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Abhay Kumar", "15/08/1950", null, Gender.TRANSGENDER)
    val ongoingAddress = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber("3.14159", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPatientEntry = OngoingPatientEntry(ongoingPersonalDetails, ongoingAddress, ongoingPhoneNumber)
    repository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd2 = OngoingPatientEntry.PersonalDetails("Alok Kumar", "15/08/1940", null, Gender.TRANSGENDER)
    val opa2 = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn2 = OngoingPatientEntry.PhoneNumber("34159", PatientPhoneNumberType.MOBILE, active = true)
    val ope2 = OngoingPatientEntry(opd2, opa2, opn2)
    repository.saveOngoingEntry(ope2)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd3 = OngoingPatientEntry.PersonalDetails("Abhishek Kumar", "1/01/1949", null, Gender.TRANSGENDER)
    val opa3 = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn3 = OngoingPatientEntry.PhoneNumber("99159", PatientPhoneNumberType.MOBILE, active = true)
    val ope3 = OngoingPatientEntry(opd3, opa3, opn3)
    repository.saveOngoingEntry(ope3)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd4 = OngoingPatientEntry.PersonalDetails("Abshot Kumar", "12/10/1951", null, Gender.TRANSGENDER)
    val opa4 = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn4 = OngoingPatientEntry.PhoneNumber("1991591", PatientPhoneNumberType.MOBILE, active = true)
    val ope4 = OngoingPatientEntry(opd4, opa4, opn4)
    repository.saveOngoingEntry(ope4)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val search0 = repository.searchPatientsAndPhoneNumbers("kumar", 12, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search0).hasSize(0)

    val search1 = repository.searchPatientsAndPhoneNumbers("kumar", 77, includeFuzzyNameSearch = false).blockingFirst()
    val person1 = search1.first()
    assertThat(search1).hasSize(1)
    assertThat(person1.fullName).isEqualTo("Alok Kumar")
    assertThat(person1.dateOfBirth).isEqualTo(LocalDate.parse("1940-08-15"))
    assertThat(person1.phoneNumber).isEqualTo("34159")

    val search2 = repository.searchPatientsAndPhoneNumbers("ab", 68, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search2).hasSize(3)
    assertThat(search2[0].fullName).isEqualTo("Abhay Kumar")
    assertThat(search2[0].dateOfBirth).isEqualTo(LocalDate.parse("1950-08-15"))
    assertThat(search2[1].fullName).isEqualTo("Abhishek Kumar")
    assertThat(search2[1].dateOfBirth).isEqualTo(LocalDate.parse("1949-01-01"))
    assertThat(search2[2].fullName).isEqualTo("Abshot Kumar")
    assertThat(search2[2].dateOfBirth).isEqualTo(LocalDate.parse("1951-10-12"))
  }

  @Test
  fun patientSearch_forPatientsWithOnlyAge_withAgeFilter_shouldReturnCorrectResults() {
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Abhay Kumar", null, "20", Gender.TRANSGENDER)
    val ongoingAddress = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val ongoingPhoneNumber = OngoingPatientEntry.PhoneNumber("3.14159", PatientPhoneNumberType.MOBILE, active = true)
    val ongoingPatientEntry = OngoingPatientEntry(ongoingPersonalDetails, ongoingAddress, ongoingPhoneNumber)
    repository.saveOngoingEntry(ongoingPatientEntry)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd2 = OngoingPatientEntry.PersonalDetails("Alok Kumar", null, "17", Gender.FEMALE)
    val opa2 = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn2 = OngoingPatientEntry.PhoneNumber("34159", PatientPhoneNumberType.MOBILE, active = true)
    val ope2 = OngoingPatientEntry(opd2, opa2, opn2)
    repository.saveOngoingEntry(ope2)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd3 = OngoingPatientEntry.PersonalDetails("Abhishek Kumar", null, "26", Gender.FEMALE)
    val opa3 = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn3 = OngoingPatientEntry.PhoneNumber("99159", PatientPhoneNumberType.MOBILE, active = true)
    val ope3 = OngoingPatientEntry(opd3, opa3, opn3)
    repository.saveOngoingEntry(ope3)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val opd4 = OngoingPatientEntry.PersonalDetails("Abshot Kumar", null, "19", Gender.FEMALE)
    val opa4 = OngoingPatientEntry.Address("Arambol", "Arambol", "Goa")
    val opn4 = OngoingPatientEntry.PhoneNumber("1991591", PatientPhoneNumberType.MOBILE, active = true)
    val ope4 = OngoingPatientEntry(opd4, opa4, opn4)
    repository.saveOngoingEntry(ope4)
        .andThen(repository.saveOngoingEntryAsPatient())
        .subscribe()

    val search0 = repository.searchPatientsAndPhoneNumbers("kumar", 50, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search0).hasSize(0)

    val search1 = repository.searchPatientsAndPhoneNumbers("kumar", 28, includeFuzzyNameSearch = false).blockingFirst()
    val person1 = search1.first()
    assertThat(search1).hasSize(1)
    assertThat(person1.fullName).isEqualTo("Abhishek Kumar")
    assertThat(person1.age!!.value).isEqualTo(26)
    assertThat(person1.phoneNumber).isEqualTo("99159")

    val search2 = repository.searchPatientsAndPhoneNumbers("ab", 18, includeFuzzyNameSearch = false).blockingFirst()
    assertThat(search2).hasSize(2)
    assertThat(search2[0].fullName).isEqualTo("Abhay Kumar")
    assertThat(search2[0].age!!.value).isEqualTo(20)
    assertThat(search2[1].fullName).isEqualTo("Abshot Kumar")
    assertThat(search2[1].age!!.value).isEqualTo(19)
  }

  @Test
  fun when_merging_patient_data_locally_it_should_also_add_them_to_the_fuzzy_search_table() {
    val patientPayloads = listOf(PatientFaker.patientPayload(fullName = "Abhaya Kumari"))

    repository.mergeWithLocalData(patientPayloads).blockingAwait()
    val searchResult = database.fuzzyPatientSearchDao().getEntriesForIds(patientPayloads.map { it.uuid }).blockingGet()
    assertThat(searchResult.size).isEqualTo(1)
    assertThat(searchResult[0].word).isEqualTo("AbhayaKumari")
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    AppDatabase.clearPatientFuzzySearchTable(database.openHelper.writableDatabase)
  }
}
