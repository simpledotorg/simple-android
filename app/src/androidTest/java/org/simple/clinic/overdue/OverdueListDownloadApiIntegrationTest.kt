package org.simple.clinic.overdue

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.overdue.download.OverdueListDownloadApi
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.util.Rules
import javax.inject.Inject

class OverdueListDownloadApiIntegrationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  @Inject
  lateinit var api: OverdueListDownloadApi

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun overdue_list_download_api_should_work_correctly() {
    // when
    val responseBody = api.download().blockingGet()

    // then
    assertThat(responseBody).isNotNull()
  }
}
