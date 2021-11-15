package org.simple.clinic.overdue.download

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.util.Rules
import javax.inject.Inject

class OverdueListDownloaderIntegrationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))

  @Inject
  lateinit var overdueListDownloader: OverdueListDownloader

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun downloading_a_csv_should_work_correctly() {
    // when
    val expectedUri = overdueListDownloader
        .download(OverdueListDownloadFormat.CSV)
        .blockingGet()

    // then
    assertThat(expectedUri).isNotNull()
  }

  @Test
  fun downloading_a_pdf_should_work_correctly() {
    // when
    val expectedUri = overdueListDownloader
        .download(OverdueListDownloadFormat.PDF)
        .blockingGet()

    // then
    assertThat(expectedUri).isNotNull()
  }
}
