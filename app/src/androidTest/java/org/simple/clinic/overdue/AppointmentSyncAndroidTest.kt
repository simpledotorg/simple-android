package org.simple.clinic.overdue

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class AppointmentSyncAndroidTest {

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  @Inject
  lateinit var repository: AppointmentRepository

  @Inject
  lateinit var dao: Appointment.RoomDao

  @Inject
  lateinit var userSession: UserSession

  @Inject
  @field:Named("last_appointment_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var sync: AppointmentSync

  @Inject
  lateinit var syncApi: AppointmentSyncApiV1

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  private fun insertDummyRecords(count: Int): Completable {
    return Observable.range(0, count)
        .map { testData.appointment(syncStatus = SyncStatus.PENDING) }
        .toList()
        .flatMapCompletable { repository.save(it) }
  }

  @Test
  fun when_pending_sync_records_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    val count = 5
    insertDummyRecords(count).blockingAwait()

    sync.push().blockingAwait()

    val updatedRecords = dao.recordsWithSyncStatus(SyncStatus.DONE).blockingFirst()
    assertThat(updatedRecords).hasSize(count)
  }

  @Test
  fun when_pulling_records_then_paginate_till_the_server_does_not_have_anymore_records() {
    lastPullTimestamp.set(Just(Instant.EPOCH))

    val recordsToInsert = 2 * configProvider.blockingGet().batchSize + 7
    val dummyPayloads = (0 until recordsToInsert).map { testData.appointmentPayload() }

    syncApi.push(AppointmentPushRequest(dummyPayloads))
        .toCompletable()
        .andThen(sync.pull())
        .blockingGet()

    val recordCountAfterPull = repository.recordCount().blockingGet()
    assertThat(recordCountAfterPull).isAtLeast(recordsToInsert)
  }
}
