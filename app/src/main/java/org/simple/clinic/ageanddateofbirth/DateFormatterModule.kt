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
  fun provideShortDigitDateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy", Locale.ENGLISH)

  @Provides
  @AppScope
  @Named("date_for_user_input")
  fun provideLongDateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
}
