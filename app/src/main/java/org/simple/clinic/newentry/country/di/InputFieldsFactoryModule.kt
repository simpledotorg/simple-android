package org.simple.clinic.newentry.country.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.EthiopiaInputFieldsProvider
import org.simple.clinic.newentry.country.IndiaInputFieldsProvider
import org.simple.clinic.newentry.country.InputFieldsProvider
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Module
class InputFieldsFactoryModule {

  @Provides
  fun provideInputFieldsProvider(
      country: Country,
      @Named("date_for_user_input") dateTimeFormatter: DateTimeFormatter,
      userClock: UserClock
  ): InputFieldsProvider {
    val date = LocalDate.now(userClock)

    return when (val isoCountryCode = country.isoCountryCode) {
      Country.INDIA -> IndiaInputFieldsProvider(dateTimeFormatter, date)
      Country.BANGLADESH -> BangladeshInputFieldsProvider(dateTimeFormatter, date)
      Country.ETHIOPIA -> EthiopiaInputFieldsProvider(dateTimeFormatter, date)
      else -> throw IllegalArgumentException("Unknown country code: $isoCountryCode")
    }
  }
}
