package org.simple.clinic.registration

import dagger.Module
import dagger.Provides
import org.simple.clinic.registration.RegistrationConfig

@Module
object RegistrationConfigModule {

  @Provides
  fun providesRegistrationConfig(): RegistrationConfig {
    return RegistrationConfig(
        showIntroVideoScreen = false
    )
  }
}
