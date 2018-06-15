package org.resolvetosavelives.red.patient

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.TestRedApp
import org.resolvetosavelives.red.patient.sync.PatientSync
import org.resolvetosavelives.red.sync.SyncConfig
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.Optional
import org.threeten.bp.Instant
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class PatientSyncAndroidTest {

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  @Inject
  lateinit var repository: PatientRepository

  @Inject
  lateinit var database: AppDatabase

  @Inject
  @field:[Named("last_patient_pull_timestamp")]
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var patientSync: PatientSync

  private val faker = Faker()
  private val oldDateFormatter = SimpleDateFormat("dd/MM/yyyy")
  private val genders = listOf(Gender.MALE, Gender.FEMALE, Gender.TRANSGENDER).shuffled()

  @Before
  fun setUp() {
    TestRedApp.appComponent().inject(this)
  }

  private fun insertDummyPatients(count: Int): Completable {
    val withDOB = Observable.range(0, count)
        .flatMapCompletable {
          repository.saveOngoingEntry(
              OngoingPatientEntry(
                  personalDetails = OngoingPatientEntry.PersonalDetails(
                      faker.name.name(),
                      oldDateFormatter.format(faker.date.between("1947-08-15", "2001-02-25")),
                      null,
                      genders.first()),
                  address = OngoingPatientEntry.Address(
                      faker.address.streetAddress(),
                      faker.address.city(),
                      faker.address.state()),
                  phoneNumber = OngoingPatientEntry.PhoneNumber(
                      faker.phoneNumber.cellPhone(),
                      PatientPhoneNumberType.LANDLINE,
                      faker.bool.bool(0.77f)
                  ))
          ).andThen(repository.saveOngoingEntryAsPatient().toCompletable())
        }

    val withoutDOB = Observable.range(0, count)
        .flatMapCompletable {
          repository
              .saveOngoingEntry(OngoingPatientEntry(
                  personalDetails = OngoingPatientEntry.PersonalDetails(
                      faker.name.name(),
                      null,
                      faker.number.number(2).toString(),
                      genders.first()),
                  address = OngoingPatientEntry.Address(
                      faker.address.streetAddress(),
                      faker.address.city(),
                      faker.address.state()),
                  phoneNumber = OngoingPatientEntry.PhoneNumber(
                      faker.phoneNumber.cellPhone(),
                      PatientPhoneNumberType.MOBILE,
                      faker.bool.bool(0.77f)
                  )))
              .andThen(repository.saveOngoingEntryAsPatient().toCompletable())
        }

    return withDOB.andThen(withoutDOB)
  }

  @Test
  fun when_pending_sync_patients_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    val count = 5
    insertDummyPatients(count).blockingAwait()

    patientSync.push().blockingAwait()

    val updatedPatients = repository.patientsWithSyncStatus(SyncStatus.DONE).blockingGet()
    assertThat(updatedPatients).hasSize(count * 2)
  }

  @Test
  fun when_pulling_patients_then_paginate_till_the_server_does_not_have_anymore_patients() {
    // Set lastPullTimestamp to a bit in the past.
    lastPullTimestamp.set(Just(Instant.now().minusSeconds(1)))

    val patientsToInsert = 2 * configProvider.blockingGet().batchSize + 7

    insertDummyPatients(count = patientsToInsert)
        .andThen(patientSync.push())
        .andThen(Completable.fromAction({ database.clearAllTables() }))
        .blockingAwait()

    patientSync.pull().blockingAwait()

    val patientCountAfterPull = repository.patientCount().blockingGet()
    assertThat(patientCountAfterPull).isAtLeast(patientsToInsert * 2)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }
}
