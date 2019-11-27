package org.simple.clinic.newentry.country

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.appconfig.Country
import org.simple.clinic.newentry.form.AgeField
import org.simple.clinic.newentry.form.BusinessIdentifierField
import org.simple.clinic.newentry.form.DateOfBirthField
import org.simple.clinic.newentry.form.DistrictField
import org.simple.clinic.newentry.form.GenderField
import org.simple.clinic.newentry.form.LandlineOrMobileField
import org.simple.clinic.newentry.form.PatientNameField
import org.simple.clinic.newentry.form.StateField
import org.simple.clinic.newentry.form.StreetAddressField
import org.simple.clinic.newentry.form.VillageOrColonyField
import org.simple.clinic.newentry.form.ZoneField
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.net.URI

class FormFieldFactoryTest {
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  private val today = LocalDate.parse("20/11/2019", dateTimeFormatter)

  @Test
  fun `it returns fields that are specific to India`() {
    val india = Country(
        isoCountryCode = "IN",
        endpoint = URI.create("https://in.simple.org/api/"),
        displayName = "India",
        isdCode = "91"
    )

    val fields = FormFieldFactory.fields(dateTimeFormatter, today, india)
    assertThat(fields.map { it::class })
        .containsExactly(
            PatientNameField::class,
            AgeField::class,
            DateOfBirthField::class,
            LandlineOrMobileField::class,
            GenderField::class,
            VillageOrColonyField::class,
            DistrictField::class,
            StateField::class
        )
        .inOrder()
  }

  @Test
  fun `it returns fields that are specific to Bangladesh`() {
    val bangladesh = Country(
        isoCountryCode = "BD",
        endpoint = URI.create("https://bd.simple.org/api/"),
        displayName = "Bangladesh",
        isdCode = "880"
    )

    val fields = FormFieldFactory.fields(dateTimeFormatter, today, bangladesh)
    assertThat(fields.map { it::class })
        .containsExactly(
            PatientNameField::class,
            AgeField::class,
            DateOfBirthField::class,
            LandlineOrMobileField::class,
            GenderField::class,
            BusinessIdentifierField::class,
            StreetAddressField::class,
            VillageOrColonyField::class,
            ZoneField::class,
            DistrictField::class,
            StateField::class
        )
        .inOrder()
  }
}
