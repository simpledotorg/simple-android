package org.simple.clinic.simplevideo

import javax.inject.Qualifier

@Qualifier
annotation class SimpleVideoConfig(val value: Type) {

  enum class Type {
    TrainingVideo
  }
}
