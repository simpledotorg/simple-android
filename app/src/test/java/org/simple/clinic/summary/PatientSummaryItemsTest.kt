package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@RunWith(JUnitParamsRunner::class)
class PatientSummaryItemsTest {

  @Test
  @Parameters(method = "params for summary item changes")
  fun `whenever summary items are changed, has item changed should return true`(
      item: PatientSummaryItems,
      changedSince: Instant,
      shouldHaveChanged: Boolean
  ) {
    assertThat(item.hasItemChangedSince(changedSince)).isEqualTo(shouldHaveChanged)
  }

  @Suppress("Unused")
  private fun `params for summary item changes`(): List<List<Any>> {
    fun generateSummaryBloodPressureListItem(syncStatus: SyncStatus, updatedAt: Instant): SummaryBloodPressureListItem {
      return SummaryBloodPressureListItem(
          measurement = PatientMocker.bp(updatedAt = updatedAt, syncStatus = syncStatus),
          daysAgo = Today,
          showDivider = false,
          formattedTime = updatedAt.toString(),
          addTopPadding = true
      )
    }

    fun generateMedicalHistoryItem(syncStatus: SyncStatus, updatedAt: Instant): SummaryMedicalHistoryItem {
      return SummaryMedicalHistoryItem(PatientMocker.medicalHistory(syncStatus = syncStatus, updatedAt = updatedAt), Today)
    }

    val now = Instant.now()
    val past = Instant.now() - Duration.ofSeconds(1)
    val future = Instant.now() + Duration.ofSeconds(1)

    return listOf(
        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            false
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            false
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = future)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = future),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = future)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(PENDING, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            false
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(PENDING, future),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(PENDING, future)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(PENDING, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            false
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, past)
            ),
            now,
            false
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, future)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(PENDING, future),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(DONE, past)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = past),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(PENDING, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, future)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = future),
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = future)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(PENDING, future),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, future)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = past),
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = past)
                    )
                ),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(PENDING, past),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, past)
            ),
            now,
            false
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(prescriptions = emptyList()),
                bloodPressureListItems = listOf(
                    generateSummaryBloodPressureListItem(DONE, past),
                    generateSummaryBloodPressureListItem(PENDING, future),
                    generateSummaryBloodPressureListItem(DONE, past)
                ),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, past)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(
                    prescriptions = listOf(
                        PatientMocker.prescription(syncStatus = PENDING, updatedAt = future),
                        PatientMocker.prescription(syncStatus = DONE, updatedAt = past)
                    )
                ),
                bloodPressureListItems = emptyList(),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, past)
            ),
            now,
            true
        ),

        listOf(
            PatientSummaryItems(
                prescriptionItems = SummaryPrescribedDrugsItem(prescriptions = emptyList()),
                bloodPressureListItems = emptyList(),
                medicalHistoryItems = generateMedicalHistoryItem(PENDING, future)
            ),
            now,
            true
        )
    )
  }
}
