package org.resolvetosavelives.red.bp

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.resolvetosavelives.red.patient.PatientFaker
import java.util.Random

class BloodPressureCategoryTest {

  @Test
  fun `compute categories`() {
    val random = { range: IntRange ->
      val rand = Random()
      range.start + rand.nextInt(range.endInclusive - range.start + 1)
    }

    val measurement1 = PatientFaker.bp(systolic = random(0..89), diastolic = random(120..500))
    assertThat(measurement1.category).isEqualTo(BloodPressureCategory.EXTREMELY_HIGH)

    val measurement2 = PatientFaker.bp(systolic = random(200..500), diastolic = random(0..59))
    assertThat(measurement2.category).isEqualTo(BloodPressureCategory.EXTREMELY_HIGH)

    val measurement3 = PatientFaker.bp(systolic = random(90..129), diastolic = random(60..79))
    assertThat(measurement3.category).isEqualTo(BloodPressureCategory.NORMAL)
  }
}
