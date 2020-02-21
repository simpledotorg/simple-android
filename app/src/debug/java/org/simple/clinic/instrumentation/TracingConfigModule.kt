package org.simple.clinic.instrumentation

import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig

@Module
class TracingConfigModule {

  @Provides
  fun provideTracingConfig(): TracingConfig = TracingConfig(true, BuildConfig.NEW_RELIC_TOKEN)
}
