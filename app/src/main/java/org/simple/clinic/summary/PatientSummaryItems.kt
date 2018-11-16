package org.simple.clinic.summary

import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant

data class PatientSummaryItems(
    val prescriptionItems: SummaryPrescribedDrugsItem,
    val bloodPressureListItems: List<SummaryBloodPressureListItem>,
    val medicalHistoryItems: SummaryMedicalHistoryItem
) {

  private fun hasChanged(syncStatus: SyncStatus, updatedAt: Instant, changedSince: Instant) =
      syncStatus == SyncStatus.PENDING && updatedAt > changedSince

  fun hasItemChangedSince(changedSince: Instant): Boolean {
    val haveDrugsBeenUpdated = prescriptionItems
        .prescriptions
        .map { hasChanged(it.syncStatus, it.updatedAt, changedSince) }
        .fold(false) { acc, hasChanged -> acc || hasChanged }

    val haveBloodPressuresBeenUpdated = bloodPressureListItems
        .map { it.measurement }
        .map { hasChanged(it.syncStatus, it.updatedAt, changedSince) }
        .fold(false) { acc, hasChanged -> acc || hasChanged }

    val hasMedicalHistoryItemBeenUpdated = hasChanged(
        medicalHistoryItems.medicalHistory.syncStatus,
        medicalHistoryItems.medicalHistory.updatedAt,
        changedSince
    )

    return haveDrugsBeenUpdated || haveBloodPressuresBeenUpdated || hasMedicalHistoryItemBeenUpdated
  }
}
