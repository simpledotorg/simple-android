package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug

sealed class TeleconsultMedicinesEvent

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultMedicinesEvent()

object EditMedicinesClicked : TeleconsultMedicinesEvent()

data class DrugDurationClicked(val prescription: PrescribedDrug) : TeleconsultMedicinesEvent()

data class DrugFrequencyClicked(val prescription: PrescribedDrug) : TeleconsultMedicinesEvent()
