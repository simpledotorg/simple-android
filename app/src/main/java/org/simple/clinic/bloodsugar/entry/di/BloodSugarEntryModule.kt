package org.simple.clinic.bloodsugar.entry.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AssistedInjectModule
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Module(includes = [AssistedInjectModule::class])
class BloodSugarEntryModule {
  @Provides
  fun provideBloodSugarDateFormatter(locale: Locale): DateTimeFormatter =
      DateTimeFormatter.ofPattern("d MMMM, yyyy", locale)
}
