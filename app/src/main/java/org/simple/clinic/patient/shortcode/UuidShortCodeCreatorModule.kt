package org.simple.clinic.patient.shortcode

import dagger.Module
import dagger.Provides

@Module
class UuidShortCodeCreatorModule {

  @Provides
  fun provideUuidShortCodeCreator(): UuidShortCodeCreator {
    val requiredShortCodeLength = 7

    return UuidShortCodeCreator(
        requiredShortCodeLength = requiredShortCodeLength,
        characterFilter = DigitFilter()
    )
  }
}
