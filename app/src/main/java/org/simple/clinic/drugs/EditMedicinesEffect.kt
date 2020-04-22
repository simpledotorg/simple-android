package org.simple.clinic.drugs

import java.util.UUID

sealed class EditMedicinesEffect

data class ShowNewPrescriptionEntrySheet(val patientUuid: UUID) : EditMedicinesEffect()
