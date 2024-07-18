package org.simple.clinic.drugs.selection.dosage

import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.widgets.UiEvent

sealed class DosagePickerEvent : UiEvent

data class DrugsLoaded(val protocolDrugs: List<ProtocolDrug>) : DosagePickerEvent()

data object NoneSelected : DosagePickerEvent() {
  override val analyticsName: String
    get() = "Protocol Drug Dosage Selection:None Selected"
}

data object ExistingPrescriptionDeleted : DosagePickerEvent()

data class DosageSelected(val protocolDrug: ProtocolDrug) : DosagePickerEvent() {
  override val analyticsName: String
    get() = "Protocol Drug Dosage Selection:Dosage Selected"
}

data object NewPrescriptionCreated : DosagePickerEvent()

data object ExistingPrescriptionChanged : DosagePickerEvent()
