package org.simple.clinic.summary.prescribeddrugs

import org.simple.clinic.drugs.OpenIntention
import org.simple.clinic.facility.Facility
import java.util.UUID

interface DrugSummaryUiActions {
  fun showUpdatePrescribedDrugsScreen(patientUuid: UUID, currentFacility: Facility, openIntention: OpenIntention)
}
