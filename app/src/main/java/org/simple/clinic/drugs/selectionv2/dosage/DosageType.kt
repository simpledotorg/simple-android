package org.simple.clinic.drugs.selectionv2.dosage

sealed class DosageType {
  data class Dosage(val dosage: String) : DosageType()
  object None: DosageType()
}
