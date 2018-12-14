package org.simple.clinic.ageanddateofbirth

import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Named

@Module
class DateFormatterModule {

  @Provides
  @AppScope
  @Named("date_for_search_results")
  fun provideDateFormatterForSearchResults(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", locale)

  @Provides
  @AppScope
  @Named("date_for_user_input")
  fun provideDateFormatterForUserInput(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
}
