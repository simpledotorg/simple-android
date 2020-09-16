package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug

sealed class TeleconsultMedicinesEvent

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultMedicinesEvent()
