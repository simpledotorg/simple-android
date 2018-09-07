package org.simple.clinic.overdue

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.sync.DataSyncAndroidTester
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class AppointmentSyncAndroidTest {

  @Inject
  lateinit var repository: AppointmentRepository

  @Inject
  @field:Named("last_appointment_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var sync: AppointmentSync

  @Inject
  lateinit var syncApi: AppointmentSyncApiV1

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var tester: DataSyncAndroidTester

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_pending_sync_records_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    tester.test_push(
        push = { sync.push() },
        repository = repository,
        generateRecord = { testData.appointment(it) })
  }

  @Test
  fun when_pulling_records_then_paginate_till_the_server_does_not_have_anymore_records() {
    tester.test_pull(
        repository = repository,
        lastPullTimestamp = lastPullTimestamp,
        pull = { sync.pull() },
        pushNetworkCall = { count ->
          val payloads = (0 until count).map { testData.appointmentPayload() }
          val request = AppointmentPushRequest(payloads)
          syncApi.push(request)
        })
  }
}
