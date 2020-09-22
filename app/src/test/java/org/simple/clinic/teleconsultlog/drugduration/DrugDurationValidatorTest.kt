package org.simple.clinic.teleconsultlog.drugduration

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration

class DrugDurationValidatorTest {

  private val config = DrugDurationConfig(
      maxAllowedDuration = Duration.ofDays(1000)
  )
  private val validator = DrugDurationValidator(
      drugDurationConfig = config
  )

  @Test
  fun `validate drug durations`() {
    assertThat(validator.validate("")).isEqualTo(Blank)
    assertThat(validator.validate("1001")).isEqualTo(MaxDrugDuration(1000))
    assertThat(validator.validate("35")).isEqualTo(Valid)
  }
}
