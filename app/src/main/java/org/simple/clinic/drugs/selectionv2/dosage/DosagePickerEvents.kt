package org.simple.clinic.drugs.selectionv2.dosage

import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class DosagePickerSheetCreated(val drugName: String, val patientUuid: UUID, val existingPrescribedDrugUuid: Optional<UUID>) : UiEvent

data class DosageItemClicked(val dosage: DosageOption) : UiEvent

data class DosageSelected(val protocolDrug: ProtocolDrug) : UiEvent
