package org.simple.clinic.overdue.download

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.overdue.download.OverdueListDownloadResult.DownloadSuccessful
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.sharedTestCode.util.Rules
import java.util.UUID
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
    val result = overdueListDownloader
        .download(OverdueListFileFormat.CSV, emptyList())
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun downloading_a_pdf_should_work_correctly() {
    // when
    val result = overdueListDownloader
        .download(OverdueListFileFormat.PDF, emptyList())
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun downloading_a_csv_for_sharing_should_work_correctly() {
    // when
    val result = overdueListDownloader
        .downloadForShare(OverdueListFileFormat.CSV)
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun downloading_a_pdf_for_sharing_should_work_correctly() {
    // when
    val result = overdueListDownloader
        .downloadForShare(OverdueListFileFormat.PDF)
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun download_a_csv_with_ids_should_work_correctly() {
    // given
    val patientId = UUID.fromString("89a1f0c4-4aa3-4f26-8f62-e2b54258da5c")

    // when
    val result = overdueListDownloader
        .download(OverdueListFileFormat.CSV, listOf(patientId))
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun download_a_pdf_with_ids_should_work_correctly() {
    // given
    val patientId = UUID.fromString("df08adb8-ea0f-42b8-bb46-179403d63383")

    // when
    val result = overdueListDownloader
        .download(OverdueListFileFormat.PDF, listOf(patientId))
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun downloading_a_csv_for_sharing_with_ids_should_work_correctly() {
    // given
    val patientId = UUID.fromString("7eebfb6c-1743-4ab3-babf-590912f5024f")

    // when
    val result = overdueListDownloader
        .downloadForShare(OverdueListFileFormat.CSV, listOf(patientId))
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun downloading_a_pdf_for_sharing_with_ids_should_work_correctly() {
    // given
    val patientId = UUID.fromString("6eb6dd13-0481-4fcf-9494-a981b66fe244")

    // when
    val result = overdueListDownloader
        .downloadForShare(OverdueListFileFormat.PDF, listOf(patientId))
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }
}
