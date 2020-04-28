package org.simple.clinic.bp.entry

import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.Validation
import javax.inject.Inject

class BpValidator @Inject constructor() {

  fun validate(systolic: String, diastolic: String): Validation {
    val systolicNumber = systolic.trim().toInt()
    val diastolicNumber = diastolic.trim().toInt()

    val reading = BloodPressureReading(
        systolic = systolicNumber,
        diastolic = diastolicNumber
    )

    return reading.validate()
  }
}
