package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.di.DateFormatter.Type.Day
import org.simple.clinic.di.DateFormatter.Type.FileDateTime
import org.simple.clinic.di.DateFormatter.Type.FullYear
import org.simple.clinic.di.DateFormatter.Type.Month
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import java.time.chrono.Chronology
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Named

@Module
class DateFormatterModule {

  @Provides
  fun providesChronology(country: Country, features: Features): Chronology {
    return if (features.isEnabled(Feature.EthiopianCalendar)) {
      country.chronology
    } else {
      IsoChronology.INSTANCE
    }
  }

  @Provides
  @Named("full_date")
  fun provideDateFormatterForFullDate(
      locale: Locale,
      chronology: Chronology,
      country: Country
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(country.fullDatePattern, locale)
        .withChronology(chronology)
  }

  @Provides
  @Named("date_for_user_input")
  fun provideDateFormatterForUserInput(locale: Locale, chronology: Chronology): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
        .withChronology(chronology)
  }

  @Provides
  @Named("time_for_bps_recorded")
  fun providesTimeFormatterForBPRecorded(
      locale: Locale,
      chronology: Chronology
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("h:mm a", locale)
        .withChronology(chronology)
  }

  @Provides
  @Named("exact_date")
  fun providesFormatterForFullReadableDate(
      locale: Locale,
      chronology: Chronology,
      country: Country
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(country.exactDatePattern, locale)
        .withChronology(chronology)
  }

  @Provides
  @Named("time_for_measurement_history")
  fun providesTimeFormatterForMeasurementHistory(
      locale: Locale,
      chronology: Chronology
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("h:mm a", locale)
        .withChronology(chronology)
  }

  @Provides
  @DateFormatter(FileDateTime)
  fun provideFormatterForFileDateTime(
      locale: Locale,
      chronology: Chronology,
      country: Country
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(country.fileDateTimePattern, locale)
        .withChronology(chronology)
  }

  @Provides
  @DateFormatter(Day)
  fun providesFormatterForDay(
      locale: Locale,
      chronology: Chronology
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("dd", locale)
        .withChronology(chronology)
  }

  @Provides
  @DateFormatter(Month)
  fun providesFormatterForMonth(
      locale: Locale,
      chronology: Chronology
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("MM", locale)
        .withChronology(chronology)
  }

  @Provides
  @DateFormatter(FullYear)
  fun providesFormatterForFullYear(
      locale: Locale,
      chronology: Chronology
  ): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("yyyy", locale)
        .withChronology(chronology)
  }
}
