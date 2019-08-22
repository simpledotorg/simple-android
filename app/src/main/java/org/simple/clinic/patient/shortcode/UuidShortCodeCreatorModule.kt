package org.simple.clinic.patient.shortcode

import dagger.Module
import dagger.Provides
import org.simple.clinic.SHORT_CODE_REQUIRED_LENGTH

@Module
class UuidShortCodeCreatorModule {

  @Provides
  fun provideUuidShortCodeCreator(): UuidShortCodeCreator {
    val requiredShortCodeLength = SHORT_CODE_REQUIRED_LENGTH

    return UuidShortCodeCreator(
        requiredShortCodeLength = requiredShortCodeLength,
        characterFilter = DigitFilter()
    )
  }
}
