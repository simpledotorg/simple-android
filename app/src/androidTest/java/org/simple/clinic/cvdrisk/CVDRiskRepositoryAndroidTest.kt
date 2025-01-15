package org.simple.clinic.cvdrisk

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import javax.inject.Inject

class CVDRiskRepositoryAndroidTest {

  @Inject
  lateinit var repository: CVDRiskRepository

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())
      .around(SaveDatabaseRule())

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_a_cvd_risk_should_work_correctly() {
    //given
    val cvdRisk = testData.cvdRisk(riskScore = CVDRiskRange(17, 24))

    //when
    repository.save(
        riskScore = cvdRisk.riskScore,
        patientUuid = cvdRisk.patientUuid,
        uuid = cvdRisk.uuid
    )

    //then
    val savedCvdRisk = repository.getCVDRiskImmediate(cvdRisk.patientUuid)!!

    assertThat(savedCvdRisk.riskScore).isEqualTo(cvdRisk.riskScore)
    assertThat(savedCvdRisk.patientUuid).isEqualTo(cvdRisk.patientUuid)
    assertThat(savedCvdRisk.uuid).isEqualTo(cvdRisk.uuid)
    assertThat(savedCvdRisk.syncStatus).isEqualTo(SyncStatus.PENDING)
  }
}
