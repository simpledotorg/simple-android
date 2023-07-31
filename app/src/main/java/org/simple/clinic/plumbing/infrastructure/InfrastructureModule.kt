package org.simple.clinic.plumbing.infrastructure

import dagger.Module
import dagger.Provides

@Module
class InfrastructureModule {

  @Provides
  fun provideInfrastructure(
      sentryInfrastructure: SentryInfrastructure,
      datadogInfrastructure: DatadogInfrastructure,
      mixpanelInfrastructure: MixpanelInfrastructure,
      firebaseAnalyticsInfrastructure: FirebaseAnalyticsInfrastructure
  ) = listOf(sentryInfrastructure, datadogInfrastructure, mixpanelInfrastructure, firebaseAnalyticsInfrastructure)
}
