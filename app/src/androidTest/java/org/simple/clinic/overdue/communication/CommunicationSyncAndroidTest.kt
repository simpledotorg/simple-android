package org.simple.clinic.overdue.communication

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
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginResult
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class CommunicationSyncAndroidTest {

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  @Inject
  lateinit var repository: CommunicationRepository

  @Inject
  lateinit var dao: Communication.RoomDao

  @Inject
  lateinit var userSession: UserSession

  @Inject
  @field:Named("last_communication_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var sync: CommunicationSync

  @Inject
  lateinit var syncApi: CommunicationSyncApiV1

  @Inject
  lateinit var testData: TestData

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    val loginResult = userSession.saveOngoingLoginEntry(testData.qaOngoingLoginEntry())
        .andThen(userSession.loginWithOtp(testData.qaUserOtp()))
        .blockingGet()
    assertThat(loginResult).isInstanceOf(LoginResult.Success::class.java)
  }

  private fun insertDummyRecords(count: Int): Completable {
    return Observable.range(0, count)
        .map { testData.communication(syncStatus = SyncStatus.PENDING) }
        .toList()
        .flatMapCompletable { repository.save(it) }
  }

  @Test
  fun when_pending_sync_records_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    val count = 5
    insertDummyRecords(count).blockingAwait()

    sync.push().blockingAwait()

    val updatedRecords = dao.withSyncStatus(SyncStatus.DONE).blockingFirst()
    assertThat(updatedRecords).hasSize(count)
  }

  @Test
  fun when_pulling_records_then_paginate_till_the_server_does_not_have_anymore_records() {
    lastPullTimestamp.set(Just(Instant.EPOCH))

    val recordsToInsert = 2 * configProvider.blockingGet().batchSize + 7
    val dummyPayloads = (0 until recordsToInsert).map { testData.communicationPayload() }

    syncApi.push(CommunicationPushRequest(dummyPayloads))
        .toCompletable()
        .andThen(sync.pull())
        .blockingGet()

    val recordCountAfterPull = dao.count()
    assertThat(recordCountAfterPull).isAtLeast(recordsToInsert)
  }

  @After
  fun tearDown() {
    userSession.logout().blockingAwait()
  }
}
