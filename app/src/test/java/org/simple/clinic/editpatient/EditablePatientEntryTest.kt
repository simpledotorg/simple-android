package org.simple.clinic.editpatient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.patient.Age
import org.simple.clinic.util.TestUtcClock
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class EditablePatientEntryTest {
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val clock: Clock = TestUtcClock()
  private val saveButtonState = EditPatientState.NOT_SAVING_PATIENT

  @Test
  fun `when the patient has an age, then entry object should have age`() {
    val patientEntry = EditablePatientEntry.from(
        TestData.patient().copy(age = Age(99, Instant.now(clock)), dateOfBirth = null),
        TestData.patientAddress(),
        null,
        dateOfBirthFormat,
        null,
        saveButtonState
    )

    assertThat(patientEntry.ageOrDateOfBirth)
        .isInstanceOf(EntryWithAge::class.java)
  }

  @Test
  fun `when the patient has a date of birth, then entry object should have date of birth`() {
    val patientEntry = EditablePatientEntry.from(
        TestData.patient().copy(age = null, dateOfBirth = LocalDate.now(clock)),
        TestData.patientAddress(),
        null,
        dateOfBirthFormat,
        null,
        saveButtonState
    )

    assertThat(patientEntry.ageOrDateOfBirth)
        .isInstanceOf(EntryWithDateOfBirth::class.java)
  }

  @Test
  fun `when the patient has both age and date of birth, then entry object should pick date of birth`() {
    val patientEntry = EditablePatientEntry.from(
        TestData.patient().copy(age = Age(99, Instant.now(clock)), dateOfBirth = LocalDate.now(clock)),
        TestData.patientAddress(),
        null,
        dateOfBirthFormat,
        null,
        saveButtonState
    )

    assertThat(patientEntry.ageOrDateOfBirth)
        .isInstanceOf(EntryWithDateOfBirth::class.java)
  }

  @Test(expected = IllegalStateException::class)
  fun `when the patient does not have neither age nor date of birth, then throw an exception`() {
    EditablePatientEntry.from(
        TestData.patient().copy(age = null, dateOfBirth = null),
        TestData.patientAddress(),
        null,
        dateOfBirthFormat,
        null,
        saveButtonState
    )
  }
}
