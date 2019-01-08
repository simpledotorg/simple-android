package org.simple.clinic.drugs.selectionv2.dosage

sealed class DosageOption {
  data class Dosage(val dosage: String) : DosageOption()
  object None: DosageOption()
}
