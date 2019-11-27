package org.simple.clinic.newentry.country

import org.simple.clinic.appconfig.Country
import org.simple.clinic.newentry.form.AgeField
import org.simple.clinic.newentry.form.DateOfBirthField
import org.simple.clinic.newentry.form.DistrictField
import org.simple.clinic.newentry.form.GenderField
import org.simple.clinic.newentry.form.InputField
import org.simple.clinic.newentry.form.LandlineOrMobileField
import org.simple.clinic.newentry.form.PatientNameField
import org.simple.clinic.newentry.form.StateField
import org.simple.clinic.newentry.form.VillageOrColonyField
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

object FormFieldFactory {
  fun fields(
      dateTimeFormatter: DateTimeFormatter,
      today: LocalDate,
      country: Country
  ): List<InputField<*>> {
    return listOf(
        PatientNameField(),
        AgeField(),
        DateOfBirthField(dateTimeFormatter, today),
        LandlineOrMobileField(),
        GenderField(),
        VillageOrColonyField(),
        DistrictField(),
        StateField()
    )
  }
}
