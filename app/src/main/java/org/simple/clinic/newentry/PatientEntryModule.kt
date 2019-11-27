package org.simple.clinic.newentry

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.newentry.form.InputField
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Named

@Module
class PatientEntryModule {
  @Provides
  fun inputFieldsFactory(@Named("date_for_user_input") dateTimeFormatter: DateTimeFormatter): InputFieldsFactory =
      InputFieldsFactory(dateTimeFormatter, LocalDate.now())

  @Provides
  fun formFields(inputFieldsFactory: InputFieldsFactory, country: Country): List<InputField<*>> =
      inputFieldsFactory.fieldsFor(country)
}
