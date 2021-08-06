package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.DateOfBirth.Type.EXACT
import org.simple.clinic.patient.DateOfBirth.Type.FROM_AGE
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.PatientSearchResultItemView
import java.time.Instant
import java.time.LocalDate
import java.time.Period

/**
 * This is a class that is meant to encapsulate dealing with the intricacies of the [Age] and the
 * [Patient.dateOfBirth] being mutually exclusive. This class will provide an easy way to access
 * the methods to estimate the patient's current age from these values without having to manually
 * check in different places.
 *
 * The end goal is to replace the [Age] and [Patient.dateOfBirth] database fields with this class
 * as an [Embedded] model.
 **/
@Parcelize
data class DateOfBirth(
    @ColumnInfo(name = "age_value")
    val ageValue: Int?,

    @ColumnInfo(name = "age_updatedAt")
    val ageUpdatedAt: Instant?,

    @ColumnInfo(name = "dateOfBirth")
    val dateOfBirth: LocalDate?
): Parcelable {

  val type: Type
    get() = when {
      dateOfBirth != null -> EXACT
      ageValue != null && ageUpdatedAt != null -> FROM_AGE
      else -> throw IllegalStateException("Could not infer type from [Age: $ageValue, Age updated: $ageUpdatedAt, DOB: $dateOfBirth]")
    }

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

  private fun calculateApproximateDateOfBirthFromRecordedAge(userClock: UserClock): LocalDate {
    val ageRecordedAtDate = ageUpdatedAt!!.atZone(userClock.zone)
    return ageRecordedAtDate.minusYears(ageValue!!.toLong()).toLocalDate()
  }

  fun withAge(age: Age): DateOfBirth {
    return copy(
        ageValue = age.value,
        ageUpdatedAt = age.updatedAt,
        dateOfBirth = null
    )
  }

  fun withDateOfBirth(dateOfBirth: LocalDate): DateOfBirth {
    return copy(
        ageValue = null,
        ageUpdatedAt = null,
        dateOfBirth = dateOfBirth
    )
  }

  // TODO: VS (24 Sep 2019) - Remove these when DateOfBirth becomes an embedded Room model
  companion object {

    fun fromPatientSearchResultViewModel(
        viewModel: PatientSearchResultItemView.PatientSearchResultViewModel,
        userClock: UserClock
    ): DateOfBirth {
      return fromAgeOrDate(viewModel.age, viewModel.dateOfBirth)
    }

    fun fromRecentPatient(recentPatient: RecentPatient, userClock: UserClock): DateOfBirth {
      return fromAgeOrDate(recentPatient.age, recentPatient.dateOfBirth)
    }

    fun fromAgeOrDate(age: Age?, date: LocalDate?): DateOfBirth {
      return when {
        date != null -> DateOfBirth(null, null, date)
        age != null -> DateOfBirth(age.value, age.updatedAt, null)
        else -> throw IllegalStateException("Both age AND dateOfBirth cannot be null!")
      }
    }
  }

  enum class Type {
    EXACT, FROM_AGE
  }
}
