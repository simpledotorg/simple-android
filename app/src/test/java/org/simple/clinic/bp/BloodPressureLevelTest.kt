package org.simple.clinic.bp

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import java.util.Random

class BloodPressureLevelTest {

  @Test
  fun `should correctly compute levels`() {
    val random = { range: IntRange ->
      val rand = Random()
      range.start + rand.nextInt(range.endInclusive - range.start + 1)
    }

    val measurement1 = TestData.bloodPressureMeasurement(systolic = random(0..89), diastolic = random(120..500))
    assertThat(measurement1.level).isEqualTo(BloodPressureLevel.EXTREMELY_HIGH)

    val measurement2 = TestData.bloodPressureMeasurement(systolic = random(200..500), diastolic = random(0..59))
    assertThat(measurement2.level).isEqualTo(BloodPressureLevel.EXTREMELY_HIGH)

    val measurement3 = TestData.bloodPressureMeasurement(systolic = random(90..129), diastolic = random(60..79))
    assertThat(measurement3.level).isEqualTo(BloodPressureLevel.NORMAL)
  }
}
