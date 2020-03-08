package org.simple.clinic.reports

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.util.None
import org.simple.clinic.util.Rules
import org.simple.clinic.util.unwrapJust
import javax.inject.Inject


class ReportsSyncAndroidTest {

  @Inject
  lateinit var reportsRepository: ReportsRepository

  @Inject
  lateinit var reportsSync: ReportsSync

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    val (reportsFile) = reportsRepository.reportsFile().blockingFirst()
    reportsFile?.delete()
  }

  @Test
  fun when_pulling_reports_from_the_server_it_should_save_the_reports_as_a_file() {
    val reportsFileBeforeSync = reportsRepository.reportsFile().blockingFirst()

    reportsSync
        .pull()
        .test()
        .assertNoErrors()

    val reportsFileAfterSync = reportsRepository.reportsFile().unwrapJust().blockingFirst()

    assertThat(reportsFileBeforeSync).isSameInstanceAs(None)
    assertThat(reportsFileAfterSync.length()).isGreaterThan(0L)
  }
}
