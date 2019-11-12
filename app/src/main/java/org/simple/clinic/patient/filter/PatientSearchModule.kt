package org.simple.clinic.patient.filter

import dagger.Module
import dagger.Provides

@Module
class PatientSearchModule {

  @Provides
  fun provideFilterPatientByName(): SearchPatientByName = WeightedLevenshteinSearch(
      minimumSearchTermLength = 3,
      maximumAllowedEditDistance = 350F,

      // Values are taken from what sqlite spellfix uses internally.
      characterSubstitutionCost = 150F,
      characterDeletionCost = 100F,
      characterInsertionCost = 100F,

      resultsComparator = SortByWeightedNameParts()
  )
}
