package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.di.DateFormatter.Type.FileDateTime
import org.threeten.extra.chrono.EthiopicChronology
import java.time.chrono.Chronology
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Named

@Module
class DateFormatterModule {

  @Provides
  fun providesChronology(country: Country): Chronology {
    return when (country.isoCountryCode) {
      Country.ETHIOPIA -> EthiopicChronology.INSTANCE
      else -> IsoChronology.INSTANCE
    }
  }

  @Provides
  @Named("full_date")
  fun provideDateFormatterForFullDate(
      locale: Locale,
      chronology: Chronology,
      country: Country
  ): DateTimeFormatter {
    val pattern = when (country.isoCountryCode) {
      Country.ETHIOPIA -> "d-MM-yyyy"
      else -> "d-MMM-yyyy"
    }
    return DateTimeFormatter.ofPattern(pattern, locale)
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
  fun providesTimeFormatterForBPRecorded(locale: Locale, chronology: Chronology): DateTimeFormatter {
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
    val pattern = when (country.isoCountryCode) {
      Country.ETHIOPIA -> "d-MM-yyyy"
      else -> "d MMMM, yyyy"
    }

    return DateTimeFormatter.ofPattern(pattern, locale)
        .withChronology(chronology)
  }

  @Provides
  @Named("time_for_measurement_history")
  fun providesTimeFormatterForMeasurementHistory(locale: Locale, chronology: Chronology): DateTimeFormatter {
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
    val pattern = when (country.isoCountryCode) {
      Country.ETHIOPIA -> "d-MM-yyyy h.mm.ss a"
      else -> "d MMM yyyy h.mm.ss a"
    }

    return DateTimeFormatter.ofPattern(pattern, locale)
        .withChronology(chronology)
  }
}
