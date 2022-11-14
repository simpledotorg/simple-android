package org.simple.clinic.registration

import dagger.Module
import dagger.Provides

@Module
object RegistrationConfigModule {

  @Provides
  fun providesRegistrationConfig(): RegistrationConfig {
    return RegistrationConfig(
        showIntroVideoScreen = true
    )
  }
}
