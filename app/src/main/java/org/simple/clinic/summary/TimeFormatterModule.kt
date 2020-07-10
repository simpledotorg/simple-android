package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Named

@Module
class TimeFormatterModule {

  @Provides
  @AppScope
  @Named("time_format_for_bp_recordings")
  fun provideTimeFormatter(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", locale)
}
