package org.simple.clinic.bp.entry.di

import dagger.Module
import dagger.Provides
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Module
class BloodPressureEntryModule {

  @Provides
  fun provideBpEntryDateFormatter(locale: Locale): DateTimeFormatter =
      DateTimeFormatter.ofPattern("d MMMM, yyyy", locale)
}
