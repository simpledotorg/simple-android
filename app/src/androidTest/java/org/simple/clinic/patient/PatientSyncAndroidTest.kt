package org.simple.clinic.patient

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.sync.PatientPushRequest
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.patient.sync.PatientSyncApiV1
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
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
  lateinit var userSession: UserSession

  @Inject
  @field:Named("last_patient_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var patientSync: PatientSync

  @Inject
  lateinit var patientSyncApiV1: PatientSyncApiV1

  @Inject
  lateinit var faker: Faker

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule()

  private val oldDateFormatter = SimpleDateFormat("dd/MM/yyyy")
  private val genders = listOf(Gender.MALE, Gender.FEMALE, Gender.TRANSGENDER).shuffled()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
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

  private fun dummyPatientPayloads(count: Int) = (0 until count).map { testData.patientPayload() }

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
    lastPullTimestamp.set(Just(Instant.EPOCH))

    val patientsToInsert = 2 * configProvider.blockingGet().batchSize + 7

    val patientCountAfterPull = patientSyncApiV1.push(PatientPushRequest(dummyPatientPayloads(patientsToInsert)))
        .toCompletable()
        .andThen(patientSync.pull())
        .andThen(repository.patientCount())
        .blockingGet()

    assertThat(patientCountAfterPull).isAtLeast(patientsToInsert)
  }
}
