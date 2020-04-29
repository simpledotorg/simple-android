package org.simple.clinic.medicalhistory

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import java.util.UUID

class MedicalHistoryTest {

  @Test
  fun `if diabetes and hypertension is not answered then diagnosis required should be false`() {
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("0bd13198-2dca-4a06-b795-6ee45ef8572b"),
        diagnosedWithHypertension = Unanswered,
        hasDiabetes = Unanswered
    )

    assertThat(medicalHistory.diagnosisRecorded).isFalse()
  }

  @Test
  fun `if diabetes is answered and hypertension is not answered then diagnosis required should be false`() {
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("0bd13198-2dca-4a06-b795-6ee45ef8572b"),
        diagnosedWithHypertension = Unanswered,
        hasDiabetes = No
    )

    assertThat(medicalHistory.diagnosisRecorded).isFalse()
  }

  @Test
  fun `if diabetes is not answered and hypertension is answered then diagnosis required should be false`() {
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("0bd13198-2dca-4a06-b795-6ee45ef8572b"),
        diagnosedWithHypertension = Yes,
        hasDiabetes = Unanswered
    )

    assertThat(medicalHistory.diagnosisRecorded).isFalse()
  }

  @Test
  fun `if diabetes and hypertension is answered then diagnosis required should be true`() {
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("0bd13198-2dca-4a06-b795-6ee45ef8572b"),
        diagnosedWithHypertension = No,
        hasDiabetes = Yes
    )

    assertThat(medicalHistory.diagnosisRecorded).isTrue()
  }
}
