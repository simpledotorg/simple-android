package org.simple.clinic.plumbing.infrastructure

import dagger.Module
import dagger.Provides

@Module
class InfrastructureModule {

  @Provides
  fun provideInfrastructure(
      sentryInfrastructure: SentryInfrastructure,
      datadogInfrastructure: DatadogInfrastructure
  ) = listOf(sentryInfrastructure, datadogInfrastructure)
}
