package org.simple.clinic.patient

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.filter.SortByTotalSumOfDistances
import org.simple.clinic.patient.filter.WeightedLevenshteinSearch
import org.simple.clinic.patient.fuzzy.AgeFuzzer
import org.simple.clinic.patient.fuzzy.PercentageFuzzer
import org.threeten.bp.Clock

@Module
open class PatientModule {

  @Provides
  open fun provideAgeFuzzer(clock: Clock): AgeFuzzer = PercentageFuzzer(clock = clock, fuzziness = 0.2F)

  @Provides
  open fun provideFilterPatientByName(): SearchPatientByName = WeightedLevenshteinSearch(
      minimumSearchTermLength = 3,
      fuzzyStringDistanceCutoff = 350F,

      // Values are taken from what sqlite spellfix uses internally.
      characterSubstitutionCost = 150F,
      characterDeletionCost = 100F,
      characterInsertionCost = 100F,

      resultsComparator = SortByTotalSumOfDistances())

  @Provides
  open fun providePatientConfig(): Single<PatientConfig> = Single.just(PatientConfig(isFuzzySearchV2Enabled = false, limitOfSearchResults = 100))
}
