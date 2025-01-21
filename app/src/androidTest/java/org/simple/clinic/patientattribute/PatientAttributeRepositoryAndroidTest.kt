package org.simple.clinic.patientattribute

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
import org.simple.clinic.util.Rules
import javax.inject.Inject

class PatientAttributeRepositoryAndroidTest {

  @Inject
  lateinit var repository: PatientAttributeRepository

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
  fun saving_a_patient_attribute_should_work_correctly() {
    //given
    val bmiReading = BMIReading(height = 177f, weight = 64f)
    val patientAttribute = testData.patientAttribute(reading = bmiReading)

    //when
    repository.save(
        bmiReading = patientAttribute.bmiReading,
        patientUuid = patientAttribute.patientUuid,
        loggedInUserUuid = patientAttribute.userUuid,
        uuid = patientAttribute.uuid
    )

    //then
    val savedPatientAttribute = repository.getPatientAttributeImmediate(patientAttribute.patientUuid)!!
    assertThat(savedPatientAttribute.bmiReading).isEqualTo(patientAttribute.bmiReading)
    assertThat(savedPatientAttribute.patientUuid).isEqualTo(patientAttribute.patientUuid)
    assertThat(savedPatientAttribute.userUuid).isEqualTo(patientAttribute.userUuid)
    assertThat(savedPatientAttribute.uuid).isEqualTo(patientAttribute.uuid)
    assertThat(savedPatientAttribute.syncStatus).isEqualTo(SyncStatus.PENDING)
  }
}
