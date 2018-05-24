package org.resolvetosavelives.red.newentry.search

import android.app.Application
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppModule

@RunWith(AndroidJUnit4::class)
class PatientRepositoryTest {

  lateinit var patientRepository: PatientRepository
  lateinit var database: AppDatabase

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getTargetContext()
    val appModule = AppModule(context.applicationContext as Application, "red-test")

    database = appModule.appDatabase()
    patientRepository = PatientRepository(database)
  }

  @Test
  fun createAnOngoingPatientEntry_thenSaveItCompletely() {
    val ongoingAddress = OngoingPatientEntry.Address("Noida", "Gautam Buddha Nagar", "Uttar Pradesh")

    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar", "09-10-1942", null, Gender.MALE)

    val personalDetailsOnlyEntry = OngoingPatientEntry(personalDetails = ongoingPersonalDetails)

    patientRepository.saveOngoingEntry(personalDetailsOnlyEntry)
        .andThen(patientRepository.ongoingEntry())
        .map { ongoingEntry -> ongoingEntry.copy(address = ongoingAddress) }
        .flatMapCompletable { withAddress -> patientRepository.saveOngoingEntry(withAddress) }
        .andThen(patientRepository.saveOngoingEntryAsPatient())
        .subscribe()

    val resultsWithPratul = patientRepository.search("lakshman")
        .take(1)
        .blockingFirst()
    assertThat(resultsWithPratul).isEmpty()

    val searchAshok = patientRepository.search("ashok")
        .take(1)
        .blockingFirst()
    assertThat(searchAshok).isNotEmpty()
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }
}
