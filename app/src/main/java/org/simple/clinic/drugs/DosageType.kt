package org.simple.clinic.drugs

sealed class DosageType {
  data class Dosage(val dosage: String) : DosageType()
  object None: DosageType()
}
