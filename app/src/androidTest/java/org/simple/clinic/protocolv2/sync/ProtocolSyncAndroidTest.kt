package org.simple.clinic.protocolv2.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import javax.inject.Inject
import javax.inject.Named


class ProtocolSyncAndroidTest {

  @Inject
  lateinit var repository: ProtocolRepository

  @Inject
  lateinit var sync: ProtocolSync

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  @Named("last_protocol_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_pulling_protocols_from_server_then_paginate_till_server_has_no_records_anymore() {
    lastPullToken.set(None())

    sync.pull()

    val count = appDatabase.protocolDao().count().blockingFirst()
    assertThat(count).isAtLeast(1)
  }
}
