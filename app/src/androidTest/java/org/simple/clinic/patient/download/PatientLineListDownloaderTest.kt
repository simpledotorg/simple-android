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
    // when
    val result = patientLineListDownloader
        .download(
            fileFormat = PatientLineListFileFormat.CSV
        )
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }

  @Test
  fun downloading_a_pdf_should_work_correctly() {
    // when
    val result = patientLineListDownloader
        .download(
            fileFormat = PatientLineListFileFormat.PDF
        )
        .blockingGet()

    // then
    assertThat(result).isInstanceOf(DownloadSuccessful::class.java)
  }
}
