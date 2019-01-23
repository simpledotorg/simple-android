package org.simple.clinic.drugs.selection.dosage

import org.simple.clinic.protocol.ProtocolDrug

sealed class DosageOption {
  data class Dosage(val protocolDrug: ProtocolDrug) : DosageOption()
  object None: DosageOption()
}
