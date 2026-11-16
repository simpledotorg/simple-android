package org.simple.clinic.scanid

import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import java.util.UUID

@Parcelize
data class ScanSimpleIdScreenKey(
    val openedFrom: OpenedFrom,
    // When adding an ID to an existing patient, an ID that only this patient already has is not
    // treated as belonging to another patient.
    val patientUuidBeingEdited: UUID? = null,
    override val analyticsName: String = "ScanSimpleId"
) : ScreenKey() {

  override fun instantiateFragment() = ScanSimpleIdScreen()
}
