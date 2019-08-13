package org.simple.clinic.protocolv2.sync

import androidx.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.LocalAuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class ProtocolSyncAndroidTest {

  @Inject
  lateinit var repository: ProtocolRepository

  @Inject
  lateinit var sync: ProtocolSync

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  @field:Named("last_protocol_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  private val authenticationRule = LocalAuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_pulling_protocols_from_server_then_paginate_till_server_has_no_records_anymore() {
    lastPullToken.set(None)

    sync.pull()
        .test()
        .assertNoErrors()

    val count = appDatabase.protocolDao().count().blockingFirst()
    assertThat(count).isAtLeast(1)
  }
}
