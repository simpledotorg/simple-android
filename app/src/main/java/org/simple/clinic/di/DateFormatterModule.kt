package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.di.DateFormatter.Type.FileDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Named

@Module
class DateFormatterModule {

  @Provides
  @Named("full_date")
  fun provideDateFormatterForFullDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", locale)

  @Provides
  @Named("date_for_user_input")
  fun provideDateFormatterForUserInput(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)

  @Provides
  @Named("time_for_bps_recorded")
  fun providesTimeFormatterForBPRecorded(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", locale)

  @Provides
  @Named("exact_date")
  fun providesFormatterForFullReadableDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM, yyyy", locale)

  @Provides
  @Named("time_for_measurement_history")
  fun providesTimeFormatterForMeasurementHistory(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", locale)

  @Provides
  @DateFormatter(FileDateTime)
  fun provideFormatterForFileDateTime(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy h.mm.ss a", locale)
}
