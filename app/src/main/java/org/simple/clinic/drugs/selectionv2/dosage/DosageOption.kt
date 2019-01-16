package org.simple.clinic.drugs.selectionv2.dosage

import org.simple.clinic.protocol.ProtocolDrug

sealed class DosageOption {
  data class Dosage(val protocolDrug: ProtocolDrug) : DosageOption()
  object None: DosageOption()
}
