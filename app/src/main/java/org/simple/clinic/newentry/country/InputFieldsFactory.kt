package org.simple.clinic.newentry.country

import org.simple.clinic.appconfig.Country
import org.simple.clinic.newentry.form.AgeField
import org.simple.clinic.newentry.form.BangladeshNationalIdField
import org.simple.clinic.newentry.form.DateOfBirthField
import org.simple.clinic.newentry.form.DistrictField
import org.simple.clinic.newentry.form.GenderField
import org.simple.clinic.newentry.form.InputField
import org.simple.clinic.newentry.form.LandlineOrMobileField
import org.simple.clinic.newentry.form.PatientNameField
import org.simple.clinic.newentry.form.StateField
import org.simple.clinic.newentry.form.StreetAddressField
import org.simple.clinic.newentry.form.VillageOrColonyField
import org.simple.clinic.newentry.form.ZoneField
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class InputFieldsFactory(
    private val dateTimeFormatter: DateTimeFormatter,
    private val today: LocalDate
) {
  fun fieldsFor(country: Country): List<InputField<*>> {
    return when (country.isoCountryCode) {
      Country.INDIA -> formFieldsForIndia()
      Country.BANGLADESH -> formFieldsForBangladesh()
      else -> throw IllegalArgumentException("Unknown country code: ${country.isoCountryCode}")
    }
  }

  private fun formFieldsForIndia(): List<InputField<*>> {
    return listOf(
        PatientNameField(),
        AgeField(),
        DateOfBirthField(dateTimeFormatter, today),
        LandlineOrMobileField(),
        StreetAddressField(),
        GenderField(),
        VillageOrColonyField(),
        DistrictField(),
        StateField()
    )
  }

  private fun formFieldsForBangladesh(): List<InputField<*>> {
    return listOf(
        PatientNameField(),
        AgeField(),
        DateOfBirthField(dateTimeFormatter, today),
        LandlineOrMobileField(),
        GenderField(),
        BangladeshNationalIdField(),
        StreetAddressField(),
        VillageOrColonyField(),
        ZoneField(),
        DistrictField(),
        StateField()
    )
  }
}
