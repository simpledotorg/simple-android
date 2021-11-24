package org.simple.clinic.plumbing.infrastructure

import dagger.Module
import dagger.Provides

@Module
class InfrastructureModule {

  @Provides
  fun provideInfrastructure() = emptyList<Infrastructure>()
}
