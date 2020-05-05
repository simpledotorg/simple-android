package org.simple.clinic.drugs.selection.dosage

import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.widgets.UiEvent

data class DosageItemClicked(val dosageOption: DosageOption) : UiEvent

data class DosageSelected(val protocolDrug: ProtocolDrug) : UiEvent

object NoneSelected : UiEvent
