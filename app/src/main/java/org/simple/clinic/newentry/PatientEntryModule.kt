package org.simple.clinic.newentry

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.util.UserClock
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Named

@Module
class PatientEntryModule {
  @Provides
  fun inputFieldsFactory(
      @Named("date_for_user_input") dateTimeFormatter: DateTimeFormatter,
      userClock: UserClock
  ) = InputFieldsFactory(dateTimeFormatter, LocalDate.now(userClock))

  @Provides
  fun formFields(inputFieldsFactory: InputFieldsFactory, country: Country): InputFields =
      InputFields(inputFieldsFactory.fieldsFor(country))
}
