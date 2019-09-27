package org.simple.clinic.editpatient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.PatientMocker
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class OngoingEditPatientEntryTest {
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  @Test
  fun `when the patient has an age, then entry object should have age`() {
    val patientEntry = OngoingEditPatientEntry.from(
        PatientMocker.patient().copy(age = Age(99, Instant.now()), dateOfBirth = null),
        PatientMocker.address(),
        null,
        dateOfBirthFormat
    )

    assertThat(patientEntry.ageOrDateOfBirth)
        .isInstanceOf(EntryWithAge::class.java)
  }

  @Test
  fun `when the patient has a date of birth, then entry object should have date of birth`() {
    val patientEntry = OngoingEditPatientEntry.from(
        PatientMocker.patient().copy(age = null, dateOfBirth = LocalDate.now()),
        PatientMocker.address(),
        null,
        dateOfBirthFormat
    )

    assertThat(patientEntry.ageOrDateOfBirth)
        .isInstanceOf(EntryWithDateOfBirth::class.java)
  }

  @Test
  fun `when the patient has both age and date of birth, then entry object should pick date of birth`() {
    val patientEntry = OngoingEditPatientEntry.from(
        PatientMocker.patient().copy(age = Age(99, Instant.now()), dateOfBirth = LocalDate.now()),
        PatientMocker.address(),
        null,
        dateOfBirthFormat
    )

    assertThat(patientEntry.ageOrDateOfBirth)
        .isInstanceOf(EntryWithDateOfBirth::class.java)
  }

  @Test(expected = IllegalStateException::class)
  fun `when the patient does not have neither age nor date of birth, then throw an exception`() {
    OngoingEditPatientEntry.from(
        PatientMocker.patient().copy(age = null, dateOfBirth = null),
        PatientMocker.address(),
        null,
        dateOfBirthFormat
    )
  }
}
