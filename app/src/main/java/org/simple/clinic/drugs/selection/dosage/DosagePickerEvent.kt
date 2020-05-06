package org.simple.clinic.drugs.selection.dosage

import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.widgets.UiEvent

sealed class DosagePickerEvent : UiEvent

data class DrugsLoaded(val protocolDrugs: List<ProtocolDrug>) : DosagePickerEvent()

object NoneSelected : DosagePickerEvent() {
  override val analyticsName: String
    get() = "Protocol Drug Dosage Selection:None Selected"
}

object ExistingPrescriptionDeleted : DosagePickerEvent()

data class DosageSelected(val protocolDrug: ProtocolDrug) : DosagePickerEvent() {
  override val analyticsName: String
    get() = "Protocol Drug Dosage Selection:Dosage Selected"
}

object NewPrescriptionCreated : DosagePickerEvent()

object ExistingPrescriptionChanged : DosagePickerEvent()
