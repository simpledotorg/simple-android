package org.resolvetosavelives.red.sync

import android.app.Application
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
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

@RunWith(AndroidJUnit4::class)
class PatientSyncAndroidTest {

  private lateinit var patientSync: PatientSync
  private lateinit var repository: PatientRepository
  private lateinit var database: AppDatabase
  private lateinit var firstSyncDone: Preference<Boolean>
  private lateinit var api: PatientSyncApiV1

  @Before
  fun setUp() {
    // TODO: Setup DI for android tests instead of manually finding dependencies.
    val appContext = InstrumentationRegistry.getTargetContext().applicationContext as Application
    val networkModule = NetworkModule()
    val patientSyncModule = PatientSyncModule()
    val moshi = networkModule.moshi()
    val rxSharedPrefs = StorageModule().rxSharedPreferences(appContext)

    api = patientSyncModule.patientSyncApi(appContext, networkModule.retrofitBuilder(moshi))
    firstSyncDone = patientSyncModule.firstSyncFlag(rxSharedPrefs)
    database = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
    repository = PatientRepository(database)
    patientSync = PatientSync(api, repository, firstSyncDone)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    database.close()
  }

  @Test
  fun when_pending_sync_patients_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    Observable.range(0, 5)
        .flatMapCompletable({ count ->
          repository
              .saveOngoingEntry(OngoingPatientEntry(
                  personalDetails = OngoingPatientEntry.PersonalDetails("Ashok Kumar #$count", "12-04-1993", null, Gender.TRANSGENDER),
                  address = OngoingPatientEntry.Address("colony-or-village", "district", "state")))
              .andThen(repository.saveOngoingEntryAsPatient())
        })
        .subscribe()

    patientSync.push().blockingAwait()

    repository.pendingSyncPatients()
        .test()
        .await()
        .assertValue({ patients -> patients.isEmpty() })
        .assertComplete()
        .assertNoErrors()
  }

  @Test
  fun when_pulling_patients_then_verify_that_they_correct_get_deserialized() {
    api.pull(isFirstSync = true, recordsToRetrieve = 20)
        .test()
        .await()
        .assertValue({ response -> response.patients.isNotEmpty() })
  }
}
