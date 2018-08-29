package org.simple.clinic.facility

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginResult
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class FacilitySyncAndroidTest {

  @Inject
  lateinit var repository: FacilityRepository

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var userSession: UserSession

  @Inject
  @field:[Named("last_facility_pull_timestamp")]
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var facilitySync: FacilitySync

  @Inject
  lateinit var testData: TestData

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    val loginResult = userSession.saveOngoingLoginEntry(testData.qaOngoingLoginEntry())
        .andThen(userSession.loginWithOtp("0000"))
        .blockingGet()
    assertThat(loginResult).isInstanceOf(LoginResult.Success::class.java)
  }

  @Test
  fun when_pulling_facilities_then_paginate_till_the_server_does_not_have_anymore_facilities() {
    lastPullTimestamp.set(Just(Instant.EPOCH))

    facilitySync.pull()
        .test()
        .assertNoErrors()

    val count = database.facilityDao().count().blockingFirst()
    assertThat(count).isAtLeast(1)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    userSession.logout().blockingAwait()
  }
}
