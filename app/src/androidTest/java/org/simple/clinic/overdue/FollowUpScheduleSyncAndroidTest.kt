package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.PatientFaker
import org.simple.clinic.TestClinicApp
import org.simple.clinic.login.LoginResult
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class FollowUpScheduleSyncAndroidTest {

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  @Inject
  lateinit var repository: FollowUpScheduleRepository

  @Inject
  lateinit var dao: FollowUpSchedule.RoomDao

  @Inject
  lateinit var userSession: UserSession

  @Inject
  @field:[Named("last_followupschedule_pull_timestamp")]
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var sync: FollowUpScheduleSync

  @Inject
  lateinit var syncApi: FollowUpScheduleSyncApiV1

  @Inject
  lateinit var patientFaker: PatientFaker

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    val loginResult = userSession.saveOngoingLoginEntry(TestClinicApp.qaOngoingLoginEntry())
        .andThen(userSession.login("0000"))
        .blockingGet()
    Truth.assertThat(loginResult).isInstanceOf(LoginResult.Success::class.java)
  }

  private fun insertDummyPatients(count: Int): Completable {
    return Observable.range(0, count)
        .map { patientFaker.followUpSchedule(syncStatus = SyncStatus.PENDING) }
        .toList()
        .flatMapCompletable { repository.save(*it.toTypedArray()) }
  }

  private fun dummySchedulePayloads(count: Int) = (0..count).map { patientFaker.followUpSchedulePayload() }

  @Test
  fun when_pending_sync_patients_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    val count = 5
    insertDummyPatients(count).blockingAwait()

    sync.push().blockingAwait()

    val updatedPatients = dao.withSyncStatus(SyncStatus.DONE).blockingFirst()
    Truth.assertThat(updatedPatients).hasSize(count)
  }

  @Test
  fun when_pulling_patients_then_paginate_till_the_server_does_not_have_anymore_patients() {
    lastPullTimestamp.set(Just(Instant.EPOCH))

    val patientsToInsert = 2 * configProvider.blockingGet().batchSize + 7

    syncApi.push(FollowUpSchedulePushRequest(dummySchedulePayloads(patientsToInsert)))
        .toCompletable()
        .andThen(sync.pull())
        .blockingGet()

    val patientCountAfterPull = dao.count()
    Truth.assertThat(patientCountAfterPull).isAtLeast(patientsToInsert)
  }

  @After
  fun tearDown() {
    userSession.logout().blockingAwait()
  }
}
