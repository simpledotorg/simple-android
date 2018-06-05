package org.resolvetosavelives.red.sync

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.TestRedApp
import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.newentry.search.SyncStatus
import org.resolvetosavelives.red.sync.patient.PatientSync
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.Optional
import org.threeten.bp.Instant
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

  @Before
  fun setUp() {
    TestRedApp.appComponent().inject(this)
  }

  private fun insertDummyPatients(count: Int): Completable {
    return Observable.range(0, count)
        .flatMapCompletable({ index ->
          repository
              .saveOngoingEntry(OngoingPatientEntry(
                  personalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar #$index", "12-04-1993", null, Gender.TRANSGENDER),
                  address = OngoingPatientEntry.Address("colony-or-village", "district", "state")))
              .andThen(repository.saveOngoingEntryAsPatient())
        })
  }

  @Test
  fun when_pending_sync_patients_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    insertDummyPatients(count = 5).blockingAwait()

    patientSync.push().blockingAwait()

    repository.patientsWithSyncStatus(SyncStatus.DONE)
        .test()
        .await()
        .assertValue({ patients -> patients.size == 5 })
        .assertComplete()
        .assertNoErrors()
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

    val patientCountAfterPull = repository.patientCount().blockingFirst()
    assertThat(patientCountAfterPull).isAtLeast(patientsToInsert)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    database.close()
  }
}
