package org.simple.clinic.drugs.selection.dosage

import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class DosagePickerSheetCreated(val existingPrescribedDrugUuid: Optional<UUID>) : UiEvent

data class DosageItemClicked(val dosageOption: DosageOption) : UiEvent

data class DosageSelected(val protocolDrug: ProtocolDrug) : UiEvent

object NoneSelected : UiEvent
