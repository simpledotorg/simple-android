package org.simple.clinic.teleconsultlog.prescription.medicines

import java.util.UUID

sealed class TeleconsultMedicinesEffect

data class LoadPatientMedicines(val patientUuid: UUID) : TeleconsultMedicinesEffect()

data class OpenEditMedicines(val patientUuid: UUID) : TeleconsultMedicinesEffect()
