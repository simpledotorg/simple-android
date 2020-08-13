package org.simple.clinic.newentry

import dagger.Module
import dagger.Provides
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Module
class PatientEntryModule {
  @Provides
  fun inputFieldsFactory(
      @Named("date_for_user_input") dateTimeFormatter: DateTimeFormatter,
      userClock: UserClock
  ) = InputFieldsFactory(dateTimeFormatter, LocalDate.now(userClock))
}
