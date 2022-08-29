package org.simple.clinic.overdue.download

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.opencsv.CSVWriter
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.OverdueCsvTitleDateTime
import org.simple.clinic.di.DateFormatter.Type.OverduePatientRegistrationDate
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.download.OverdueCsvColumn.AGE
import org.simple.clinic.overdue.download.OverdueCsvColumn.BP_PASSPORT_NUMBER
import org.simple.clinic.overdue.download.OverdueCsvColumn.DAYS_OVERDUE
import org.simple.clinic.overdue.download.OverdueCsvColumn.GENDER
import org.simple.clinic.overdue.download.OverdueCsvColumn.LAST_MEDICINES
import org.simple.clinic.overdue.download.OverdueCsvColumn.PATIENT_ADDRESS
import org.simple.clinic.overdue.download.OverdueCsvColumn.PATIENT_NAME
import org.simple.clinic.overdue.download.OverdueCsvColumn.PATIENT_PHONE
import org.simple.clinic.overdue.download.OverdueCsvColumn.PATIENT_VILLAGE_OR_COLONY
import org.simple.clinic.overdue.download.OverdueCsvColumn.REGISTRATION_DATE
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

private enum class OverdueCsvColumn(val description: String) {
  REGISTRATION_DATE(description = "Registration date"),
  BP_PASSPORT_NUMBER(description = "BP Passport number"),
  PATIENT_NAME(description = "Patient name"),
  GENDER(description = "Gender"),
  AGE(description = "Age"),
  PATIENT_ADDRESS(description = "Patient address"),
  PATIENT_VILLAGE_OR_COLONY(description = "Patient village or colony"),
  DAYS_OVERDUE(description = "Days overdue"),
  PATIENT_PHONE(description = "Patient phone"),
  LAST_MEDICINES(description = "Latest medicines")
}

class OverdueCsvGenerator @Inject constructor(
    private val userClock: UserClock,
    private val utcClock: UtcClock,
    @DateFormatter(OverdueCsvTitleDateTime)
    private val overdueCsvTitleDateTimeFormatter: DateTimeFormatter,
    @DateFormatter(OverduePatientRegistrationDate)
    private val patientRegistrationDateFormatter: DateTimeFormatter,
    private val appointmentRepository: AppointmentRepository
) {

  fun generate(ids: List<UUID>): ByteArrayOutputStream {
    val outputStream = ByteArrayOutputStream()
    val csvWriter = CSVWriter(OutputStreamWriter(outputStream))

    val now = LocalDateTime.now(userClock)
    val overdueDownloadDateAndTime = overdueCsvTitleDateTimeFormatter.format(now)

    val cursor = appointmentRepository.appointmentAndPatientInformationForIds(ids)
    val columns = OverdueCsvColumn.values()

    csvWriter.writeNext(arrayOf("Overdue list downloaded at: $overdueDownloadDateAndTime"), false)
    csvWriter.writeNext(columns.map { it.description }.toTypedArray(), false)

    while (cursor.moveToNext()) {
      val line = readOverdueCursor(cursor, columns, now)
      csvWriter.writeNext(line, false)
    }

    csvWriter.close()
    cursor.close()

    return outputStream
  }

  private fun readOverdueCursor(
      cursor: Cursor,
      overdueCsvColumns: Array<OverdueCsvColumn>,
      now: LocalDateTime
  ): Array<String?> {
    val columns = arrayOfNulls<String>(overdueCsvColumns.size)

    overdueCsvColumns.forEachIndexed { index, column ->
      val value: String = when (column) {
        REGISTRATION_DATE -> readPatientRegistrationDate(cursor)
        BP_PASSPORT_NUMBER -> readBpPassportNumber(cursor)
        PATIENT_NAME -> readPatientName(cursor)
        GENDER -> readPatientGender(cursor)
        AGE -> readPatientAge(cursor)
        PATIENT_ADDRESS -> readPatientAddress(cursor)
        PATIENT_VILLAGE_OR_COLONY -> readPatientVillageOrColony(cursor)
        DAYS_OVERDUE -> readDaysOverdue(cursor, now)
        PATIENT_PHONE -> readPatientPhoneNumber(cursor)
        LAST_MEDICINES -> readPrescribedDrugs(cursor)
      }

      columns[index] = value
    }

    return columns
  }

  private fun readPrescribedDrugs(cursor: Cursor): String {
    return cursor.getStringOrNull(cursor.getColumnIndexOrThrow("prescribedDrugs")).orEmpty()
  }

  private fun readPatientPhoneNumber(cursor: Cursor): String {
    return cursor.getStringOrNull(cursor.getColumnIndexOrThrow("patientPhoneNumber")).orEmpty()
  }

  private fun readDaysOverdue(cursor: Cursor, now: LocalDateTime): String {
    val appointmentScheduledDateString = cursor.getString(cursor.getColumnIndexOrThrow("appointmentScheduledAt"))
    val appointmentScheduledDate = LocalDate.parse(appointmentScheduledDateString)

    return ChronoUnit.DAYS.between(appointmentScheduledDate, now).toString()
  }

  private fun readPatientVillageOrColony(cursor: Cursor): String {
    return cursor.getStringOrNull(cursor.getColumnIndexOrThrow("patientColonyOrVillage")).orEmpty()
  }

  private fun readPatientAddress(cursor: Cursor): String {
    return cursor.getStringOrNull(cursor.getColumnIndexOrThrow("patientStreetAddress")).orEmpty()
  }

  private fun readPatientAge(cursor: Cursor): String {
    val patientAge = cursor.getIntOrNull(cursor.getColumnIndexOrThrow("patientAgeValue"))
    val patientUpdatedAtString = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("patientAgeUpdatedAt"))
    val patientUpdatedAt = if (!patientUpdatedAtString.isNullOrBlank()) {
      Instant.parse(patientUpdatedAtString)
    } else {
      null
    }
    val patientDateOfBirthString = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("patientDateOfBirth"))
    val patientDateOfBirth = if (!patientDateOfBirthString.isNullOrBlank()) {
      LocalDate.parse(patientDateOfBirthString)
    } else {
      null
    }
    val patientAgeDetails = PatientAgeDetails(
        ageValue = patientAge,
        ageUpdatedAt = patientUpdatedAt,
        dateOfBirth = patientDateOfBirth
    )

    return patientAgeDetails.estimateAge(userClock).toString()
  }

  private fun readPatientGender(cursor: Cursor): String {
    val genderString = cursor.getString(cursor.getColumnIndexOrThrow("patientGender"))

    return when (val gender = Gender.TypeAdapter.toEnum(genderString)) {
      Gender.Female -> "Female"
      Gender.Male -> "Male"
      Gender.Transgender -> "Transgender"
      is Gender.Unknown -> gender.actualValue
      null -> ""
    }
  }

  private fun readPatientName(cursor: Cursor) = cursor.getString(cursor.getColumnIndexOrThrow("patientName"))

  private fun readBpPassportNumber(cursor: Cursor): String {
    val bpPassportUuid = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("identifierValue"))
    return if (!bpPassportUuid.isNullOrBlank()) {
      val identifier = Identifier(
          value = bpPassportUuid,
          type = Identifier.IdentifierType.BpPassport
      )

      identifier.displayValue()
    } else {
      ""
    }
  }

  private fun readPatientRegistrationDate(cursor: Cursor): String {
    val date = cursor.getString(cursor.getColumnIndexOrThrow("patientCreatedAt"))
    val patientRegistrationDate = Instant.parse(date).toLocalDateAtZone(utcClock.zone)

    return patientRegistrationDateFormatter.format(patientRegistrationDate)
  }
}
