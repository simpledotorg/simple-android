package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Named

@Module
class DateFormatterModule {

  @Provides
  @Named("date_for_search_results")
  fun provideDateFormatterForSearchResults(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", locale)

  @Provides
  @Named("date_for_user_input")
  fun provideDateFormatterForUserInput(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)

  @Provides
  @Named("time_for_bps_recorded")
  fun providesTimeFormatterForBPRecorded(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", locale)

  @Provides
  @Named("exact_date")
  fun providesTimeFormatterForRecentPatientsHeader(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM, yyyy", locale)
}
