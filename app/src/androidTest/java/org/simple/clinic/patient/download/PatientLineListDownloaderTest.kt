package org.simple.clinic.patient.download

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.patient.download.PatientLineListDownloadResult.DownloadSuccessful
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.sharedTestCode.util.Rules
import java.time.LocalDate
import javax.inject.Inject

class PatientLineListDownloaderTest {

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
      .around(LocalAuthenticationRule())

  @Inject
  lateinit var patientLineListDownloader: PatientLineListDownloader

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun downloading_a_csv_should_work_correctly() {
    // given
    val bpCreatedAfter = LocalDate.parse("2018-01-01")
    val bpCreatedBefore = LocalDate.parse("2018-03-01")

    // when
    val result = patientLineListDownloader
        .download(
            bpCreatedAfter = bpCreatedAfter,
            bpCreatedBefore = bpCreatedBefore,
            fileFormat = PatientLineListFileFormat.CSV
        )
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun downloading_a_pdf_should_work_correctly() {
    // given
    val bpCreatedAfter = LocalDate.parse("2018-01-01")
    val bpCreatedBefore = LocalDate.parse("2018-03-01")

    // when
    val result = patientLineListDownloader
        .download(
            bpCreatedAfter = bpCreatedAfter,
            bpCreatedBefore = bpCreatedBefore,
            fileFormat = PatientLineListFileFormat.PDF
        )
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }
}
