package org.simple.clinic.patient

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.filter.SortByWeightedNameParts
import org.simple.clinic.patient.filter.WeightedLevenshteinSearch
import org.simple.clinic.patient.fuzzy.AgeFuzzer
import org.simple.clinic.patient.fuzzy.PercentageFuzzer
import org.simple.clinic.util.UtcClock

@Module
open class PatientModule {

  @Provides
  open fun provideAgeFuzzer(utcClock: UtcClock): AgeFuzzer = PercentageFuzzer(utcClock = utcClock, fuzziness = 0.2F)

  @Provides
  open fun provideFilterPatientByName(): SearchPatientByName = WeightedLevenshteinSearch(
      minimumSearchTermLength = 3,
      maximumAllowedEditDistance = 350F,

      // Values are taken from what sqlite spellfix uses internally.
      characterSubstitutionCost = 150F,
      characterDeletionCost = 100F,
      characterInsertionCost = 100F,

      resultsComparator = SortByWeightedNameParts())

  @Provides
  open fun providePatientConfig(): Observable<PatientConfig> = Observable.just(PatientConfig(
      limitOfSearchResults = 100,
      scanSimpleCardFeatureEnabled = false
  ))
}
