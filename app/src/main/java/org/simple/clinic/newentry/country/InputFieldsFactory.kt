package org.simple.clinic.newentry.country

import org.simple.clinic.R
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
        PatientNameField(R.string.patiententry_full_name),
        AgeField(R.string.patiententry_age),
        DateOfBirthField(dateTimeFormatter, today, R.string.patiententry_date_of_birth_unfocused),
        LandlineOrMobileField(R.string.patiententry_phone_number),
        StreetAddressField(R.string.patiententry_street_address),
        GenderField(labelResId = 0),
        VillageOrColonyField(R.string.patiententry_colony_or_village),
        DistrictField(R.string.patiententry_district),
        StateField(R.string.patiententry_state)
    )
  }

  private fun formFieldsForBangladesh(): List<InputField<*>> {
    return listOf(
        PatientNameField(R.string.patiententry_full_name),
        AgeField(R.string.patiententry_age),
        DateOfBirthField(dateTimeFormatter, today, R.string.patiententry_date_of_birth_unfocused),
        LandlineOrMobileField(R.string.patiententry_phone_number),
        GenderField(labelResId = 0),
        BangladeshNationalIdField(R.string.patiententry_bangladesh_national_id),
        StreetAddressField(R.string.patiententry_street_address),
        VillageOrColonyField(R.string.patiententry_colony_or_village),
        ZoneField(R.string.patiententry_zone),
        DistrictField(R.string.patiententry_district),
        StateField(R.string.patiententry_state)
    )
  }
}
