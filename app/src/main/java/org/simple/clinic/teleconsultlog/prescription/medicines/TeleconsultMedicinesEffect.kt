package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.time.Duration
import java.util.UUID

sealed class TeleconsultMedicinesEffect

data class LoadPatientMedicines(val patientUuid: UUID) : TeleconsultMedicinesEffect()

data class OpenEditMedicines(val patientUuid: UUID) : TeleconsultMedicinesEffect()

data class OpenDrugDurationSheet(val prescription: PrescribedDrug) : TeleconsultMedicinesEffect()

data class OpenDrugFrequencySheet(val prescription: PrescribedDrug) : TeleconsultMedicinesEffect()

data class UpdateDrugDuration(
    val prescribedDrugUuid: UUID,
    val duration: Duration
) : TeleconsultMedicinesEffect()

data class UpdateDrugFrequency(
    val prescribedDrugUuid: UUID,
    val drugFrequency: MedicineFrequency
) : TeleconsultMedicinesEffect()
