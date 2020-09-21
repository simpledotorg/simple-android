package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.time.Duration

data class TeleconsultMedicinesConfig(
    val defaultDuration: Duration,
    val defaultFrequency: MedicineFrequency
)
