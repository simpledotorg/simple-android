package org.simple.clinic.newentry.country

import dagger.Lazy
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.newentry.form.AgeField
import org.simple.clinic.newentry.form.DateOfBirthField
import org.simple.clinic.newentry.form.DistrictField
import org.simple.clinic.newentry.form.GenderField
import org.simple.clinic.newentry.form.InputField
import org.simple.clinic.newentry.form.LandlineOrMobileField
import org.simple.clinic.newentry.form.PatientNameField
import org.simple.clinic.newentry.form.StateField
import org.simple.clinic.newentry.form.StreetAddressField
import org.simple.clinic.newentry.form.VillageOrColonyField
import org.simple.clinic.patient.Gender
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class IndiaInputFieldsProvider(
    private val dateTimeFormatter: DateTimeFormatter,
    private val today: LocalDate,
    private val currentFacility: Lazy<Facility>,
    private val chennaiFacilityGroupIds: Lazy<Set<UUID>>
) : InputFieldsProvider {

  override fun provide(): List<InputField<*>> {
    val currentFacilityGroupId = currentFacility.get().groupUuid

    return listOf(
        PatientNameField(R.string.patiententry_full_name),
        AgeField(R.string.patiententry_age),
        DateOfBirthField(
            { value -> LocalDate.parse(value, dateTimeFormatter) },
            today,
            R.string.patiententry_date_of_birth_unfocused
        ),
        LandlineOrMobileField(R.string.patiententry_phone_number),
        StreetAddressField(selectStreetAddressLabel(currentFacilityGroupId)),
        GenderField(_labelResId = 0, allowedGenders = setOf(Gender.Male, Gender.Female, Gender.Transgender)),
        VillageOrColonyField(selectVillageOrColonyLabel(currentFacilityGroupId)),
        DistrictField(R.string.patiententry_district),
        StateField(R.string.patiententry_state)
    )
  }

  private fun selectStreetAddressLabel(currentFacilityGroupId: UUID?): Int {
    return if (currentFacilityGroupId in chennaiFacilityGroupIds.get())
      R.string.patiententry_doornumber_streetname
    else
      R.string.patiententry_street_address
  }

  private fun selectVillageOrColonyLabel(currentFacilityGroupId: UUID?): Int {
    return if (currentFacilityGroupId in chennaiFacilityGroupIds.get())
      R.string.patiententry_division_area
    else
      R.string.patiententry_village_colony_ward
  }
}
