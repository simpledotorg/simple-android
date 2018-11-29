package org.simple.clinic.editpatient

import dagger.Module
import dagger.Provides
import io.reactivex.Single

@Module
open class PatientEditModule {

  @Provides
  open fun providePatientEditConfig(): Single<PatientEditConfig> = Single.just(PatientEditConfig(isEditAgeAndDobEnabled = true))
}
