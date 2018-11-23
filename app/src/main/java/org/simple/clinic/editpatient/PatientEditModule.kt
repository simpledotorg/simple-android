package org.simple.clinic.editpatient

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.di.AppScope

@Module
open class PatientEditModule {

  @Provides
  @AppScope
  open fun providePatientEditConfig(): Single<PatientEditConfig> = Single.just(PatientEditConfig(isEditAgeAndDobEnabled = false))
}