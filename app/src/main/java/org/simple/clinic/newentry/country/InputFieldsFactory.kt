package org.simple.clinic.newentry.country

import org.simple.clinic.appconfig.Country
import org.simple.clinic.newentry.form.InputField
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class InputFieldsFactory constructor(
    private val inputFieldsProvider: InputFieldsProvider
) {

  @Inject
  constructor(
      @Named("date_for_user_input") dateTimeFormatter: DateTimeFormatter,
      userClock: UserClock,
      country: Country
  ) : this(
      inputFieldsProvider = when (val isoCountryCode = country.isoCountryCode) {
        Country.INDIA -> IndiaInputFieldsProvider(dateTimeFormatter, LocalDate.now(userClock))
        Country.BANGLADESH -> BangladeshInputFieldsProvider(dateTimeFormatter, LocalDate.now(userClock))
        Country.ETHIOPIA -> EthiopiaInputFieldsProvider(dateTimeFormatter, LocalDate.now(userClock))
        else -> throw IllegalArgumentException("Unknown country code: $isoCountryCode")
      }
  )

  fun provideFields(): List<InputField<*>> {
    return inputFieldsProvider.provide()
  }
}
