package org.simple.clinic.summary.prescribeddrugs

import org.simple.clinic.drugs.OpenIntention
import org.simple.clinic.facility.Facility
import java.util.UUID

sealed class DrugSummaryEffect

data class LoadPrescribedDrugs(val patientUuid: UUID) : DrugSummaryEffect()

object LoadCurrentFacility : DrugSummaryEffect()

data class OpenUpdatePrescribedDrugScreen(
    val patientUuid: UUID,
    val facility: Facility,
    val openIntention: OpenIntention
) : DrugSummaryEffect()
