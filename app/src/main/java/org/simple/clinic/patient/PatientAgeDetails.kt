package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.PatientAgeDetails.Type.EXACT
import org.simple.clinic.patient.PatientAgeDetails.Type.FROM_AGE
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.time.LocalDate
import java.time.Period

/**
 * This is a class that is meant to encapsulate dealing with the intricacies of the [Age] and the
 * [Patient.dateOfBirth] being mutually exclusive. This class will provide an easy way to access
 * the methods to estimate the patient's current age from these values without having to manually
 * check in different places.
 **/
@Parcelize
data class PatientAgeDetails(
    @ColumnInfo(name = "age_value")
    val ageValue: Int?,

    @ColumnInfo(name = "age_updatedAt")
    val ageUpdatedAt: Instant?,

    @ColumnInfo(name = "dateOfBirth")
    val dateOfBirth: LocalDate?
) : Parcelable {

  val type: Type
    get() = when {
      dateOfBirth != null -> EXACT
      ageValue != null && ageUpdatedAt != null -> FROM_AGE
      else -> throw IllegalStateException("Could not infer type from [Age: $ageValue, Age updated: $ageUpdatedAt, DOB: $dateOfBirth]")
    }

  private val isRecordedAsAge: Boolean
    get() = dateOfBirth == null && (ageValue != null && ageUpdatedAt != null)

  fun estimateAge(userClock: UserClock): Int {
    return when (type) {
      EXACT -> computeCurrentAgeFromRecordedDateOfBirth(userClock)
      FROM_AGE -> computeCurrentAgeFromRecordedAge(userClock)
    }
  }

  private fun computeCurrentAgeFromRecordedDateOfBirth(userClock: UserClock): Int {
    return Period.between(dateOfBirth, LocalDate.now(userClock)).years
  }

  private fun computeCurrentAgeFromRecordedAge(userClock: UserClock): Int {
    val ageRecordedAtDate = ageUpdatedAt!!.atZone(userClock.zone).toLocalDate()
    val currentDate = LocalDate.now(userClock)

    return ageValue!! + Period.between(ageRecordedAtDate, currentDate).years
  }

  fun approximateDateOfBirth(userClock: UserClock): LocalDate {
    return when (type) {
      EXACT -> dateOfBirth!!
      FROM_AGE -> calculateApproximateDateOfBirthFromRecordedAge(userClock)
    }
  }

  fun doesRecordedAgeMatch(age: Int): Boolean {
    return isRecordedAsAge && age == ageValue!!
  }

  private fun calculateApproximateDateOfBirthFromRecordedAge(userClock: UserClock): LocalDate {
    val ageRecordedAtDate = ageUpdatedAt!!.atZone(userClock.zone)
    return ageRecordedAtDate.minusYears(ageValue!!.toLong()).toLocalDate()
  }

  fun withDateOfBirth(dateOfBirth: LocalDate): PatientAgeDetails {
    return copy(
        ageValue = null,
        ageUpdatedAt = null,
        dateOfBirth = dateOfBirth
    )
  }

  fun withUpdatedAge(
      ageValue: Int,
      clock: UtcClock
  ): PatientAgeDetails {
    return copy(
        ageValue = ageValue,
        ageUpdatedAt = Instant.now(clock),
        dateOfBirth = null
    )
  }

  enum class Type {
    EXACT, FROM_AGE
  }
}
