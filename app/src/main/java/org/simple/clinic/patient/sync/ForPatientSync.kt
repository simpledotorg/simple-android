package org.simple.clinic.patient.sync

import javax.inject.Qualifier

@Qualifier
annotation class ForPatientSync(val value: Type) {

  enum class Type {
    RecordRetentionFallbackDuration
  }
}
