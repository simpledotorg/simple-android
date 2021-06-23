package org.simple.clinic.patient

import javax.inject.Qualifier

@Qualifier
annotation class SimpleVideo(val value: Type) {

  enum class Type {
    TrainingVideoYoutubeId,
    NumberOfPatientsRegistered
  }
}
