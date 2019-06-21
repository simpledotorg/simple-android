package org.simple.clinic.allpatientsinfacility.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityUiState

@Module
class AllPatientsInFacilityModule {

  @Provides
  fun provideInitialUiState(): AllPatientsInFacilityUiState = AllPatientsInFacilityUiState.FETCHING_PATIENTS

}
