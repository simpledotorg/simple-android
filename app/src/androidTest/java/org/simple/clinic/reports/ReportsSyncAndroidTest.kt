package org.simple.clinic.reports

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.ServerAuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.unwrapJust
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ReportsSyncAndroidTest {

  @Inject
  lateinit var reportsRepository: ReportsRepository

  @Inject
  lateinit var reportsSync: ReportsSync

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(ServerAuthenticationRule())
      .around(RxErrorsRule())!!

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

    assertThat(reportsFileBeforeSync).isSameAs(None)
    assertThat(reportsFileAfterSync.length()).isGreaterThan(0L)
  }
}
