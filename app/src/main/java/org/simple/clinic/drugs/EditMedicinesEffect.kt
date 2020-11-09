package org.simple.clinic.drugs

import java.util.UUID

sealed class EditMedicinesEffect

data class ShowNewPrescriptionEntrySheet(val patientUuid: UUID) : EditMedicinesEffect()

data class OpenDosagePickerSheet(
    val drugName: String,
    val patientUuid: UUID,
    val prescribedDrugUuid: UUID?
) : EditMedicinesEffect()

data class ShowUpdateCustomPrescriptionSheet(val prescribedDrug: PrescribedDrug) : EditMedicinesEffect()

object GoBackToPatientSummary : EditMedicinesEffect()

data class FetchPrescribedAndProtocolDrugs(val patientUuid: UUID) : EditMedicinesEffect()

data class RefillMedicines(val patientUuid: UUID) : EditMedicinesEffect()


