package org.simple.clinic.patient

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.fuzzy.PercentageFuzzer
import org.threeten.bp.Clock

@Module
open class PatientSearchConfigModule {

  @AppScope
  @Provides
  open fun providePatientSearchConfig(clock: Clock): Single<PatientSearchConfig> {
    return Single.just(PatientSearchConfig(
        ageFuzzer = PercentageFuzzer(clock = clock, fuzziness = 0.2F),
        fuzzyStringDistanceCutoff = 350F,

        // Values are taken from what sqlite spellfix uses internally.
        characterSubstitutionCost = 150F,
        characterDeletionCost = 100F,
        characterInsertionCost = 100F
    ))
  }
}
