package org.simple.clinic.drugs

import org.simple.clinic.summary.DiagnosisWarningResult
import java.util.UUID

sealed class EditMedicinesEffect

data class FetchPrescribedAndProtocolDrugs(val patientUuid: UUID) : EditMedicinesEffect()

data class RefillMedicines(val patientUuid: UUID) : EditMedicinesEffect()

object LoadDrugFrequencyChoiceItems : EditMedicinesEffect()

data class LoadDataOnExiting(val patientUuid: UUID) : EditMedicinesEffect()

sealed class EditMedicinesViewEffect : EditMedicinesEffect()

data class ShowNewPrescriptionEntrySheet(val patientUuid: UUID) : EditMedicinesViewEffect()

data class OpenDosagePickerSheet(
    val drugName: String,
    val patientUuid: UUID,
    val prescribedDrugUuid: UUID?
) : EditMedicinesViewEffect()

data class ShowUpdateCustomPrescriptionSheet(val prescribedDrug: PrescribedDrug) : EditMedicinesViewEffect()

data object GoBackToPatientSummary : EditMedicinesViewEffect()

data class GoBackToPatientSummaryWithWarningResult(val diagnosisWarningResult: DiagnosisWarningResult) : EditMedicinesViewEffect()
