package org.resolvetosavelives.red.sync

import android.app.Application
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.NetworkModule
import org.resolvetosavelives.red.di.StorageModule
import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.newentry.search.SyncStatus
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@RunWith(AndroidJUnit4::class)
class PatientSyncAndroidTest {

  private lateinit var patientSync: PatientSync
  private lateinit var repository: PatientRepository
  private lateinit var database: AppDatabase
  private lateinit var lastPullTimestamp: Preference<Optional<Instant>>
  private lateinit var api: PatientSyncApiV1
  private val config = PatientSyncConfig(frequency = Duration.ofHours(1), batchSize = 10)

  @Before
  fun setUp() {
    // TODO: Setup DI for android tests instead of manually finding dependencies.
    val appContext = InstrumentationRegistry.getTargetContext().applicationContext as Application
    val networkModule = NetworkModule()
    val patientSyncModule = PatientSyncModule()
    val moshi = networkModule.moshi()
    val rxSharedPrefs = StorageModule().rxSharedPreferences(appContext)

    api = patientSyncModule.patientSyncApi(appContext, networkModule.retrofitBuilder(moshi))
    lastPullTimestamp = patientSyncModule.lastPullTimestamp(rxSharedPrefs)
    database = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
    repository = PatientRepository(database)
    patientSync = PatientSync(api, repository, Single.just(config), lastPullTimestamp)
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

    repository.patientsWithSyncStatus(SyncStatus.PENDING)
        .test()
        .await()
        .assertValue({ patients -> patients.isEmpty() })
        .assertComplete()
        .assertNoErrors()
  }

  @Test
  fun when_pulling_patients_then_paginate_till_the_server_does_not_have_anymore_patients() {
    // Set lastPullTimestamp to a bit in the past.
    lastPullTimestamp.set(Some(Instant.now().minusSeconds(1)))

    val patientsToInsert = 2 * config.batchSize + 7

    insertDummyPatients(count = patientsToInsert)
        .andThen(patientSync.push())
        .andThen(Completable.fromAction({ database.clearAllTables() }))
        .blockingAwait()

    patientSync.pull().blockingAwait()

    repository.patientCount()
        .take(1)
        .test()
        .await()
        .assertValue(patientsToInsert)
        .assertComplete()
        .assertNoErrors()
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    database.close()
  }
}
