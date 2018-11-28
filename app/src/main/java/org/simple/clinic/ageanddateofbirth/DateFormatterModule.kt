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
  @Named("short_date")
  fun provideShortDigitDateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy", Locale.ENGLISH)

  @Provides
  @AppScope
  @Named("long_date")
  fun provideLongDateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
}
