package org.simple.clinic.patient

import androidx.annotation.VisibleForTesting
import androidx.room.Embedded
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.patient.DateOfBirth.Type.EXACT
import org.simple.clinic.patient.DateOfBirth.Type.FROM_AGE
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.PatientSearchResultItemView
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

/**
 * This is a class that is meant to encapsulate dealing with the intricacies of the [Age] and the
 * [Patient.dateOfBirth] being mutually exclusive. This class will provide an easy way to access
 * the methods to estimate the patient's current age from these values without having to manually
 * check in different places.
 *
 * The end goal is to replace the [Age] and [Patient.dateOfBirth] database fields with this class
 * as an [Embedded] model.
 **/
data class DateOfBirth(
    val date: LocalDate,
    val type: Type,
    val updatedAt: Instant
) {

  fun estimateAge(userClock: UserClock): Int {
    return Period.between(date, LocalDate.now(userClock)).years
  }

  // TODO: VS (24 Sep 2019) - Remove these when DateOfBirth becomes an embedded Room model
  companion object {
    private fun fromDate(
        date: LocalDate,
        utcClock: UtcClock
    ): DateOfBirth {
      return DateOfBirth(date, EXACT, Instant.now(utcClock))
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun fromAge(
        age: Age,
        userClock: UserClock
    ): DateOfBirth {
      val ageRecordedAtDate = age.updatedAt.atZone(userClock.zone)

      val guessedDateOfBirth = ageRecordedAtDate.minusYears(age.value.toLong()).toLocalDate()

      return DateOfBirth(guessedDateOfBirth, FROM_AGE, age.updatedAt)
    }

    fun fromPatient(
        patient: Patient,
        userClock: UserClock,
        utcClock: UtcClock
    ): DateOfBirth {
      return fromAgeOrDate(patient.age, patient.dateOfBirth, userClock, utcClock)
    }

    fun fromOverdueAppointment(
        overdueAppointment: OverdueAppointment,
        userClock: UserClock,
        utcClock: UtcClock
    ): DateOfBirth {
      return fromAgeOrDate(overdueAppointment.age, overdueAppointment.dateOfBirth, userClock, utcClock)
    }

    fun fromPatientSearchResultViewModel(
        viewModel: PatientSearchResultItemView.PatientSearchResultViewModel,
        userClock: UserClock,
        utcClock: UtcClock
    ): DateOfBirth {
      return fromAgeOrDate(viewModel.age, viewModel.dateOfBirth, userClock, utcClock)
    }

    fun fromRecentPatient(
        recentPatient: RecentPatient,
        userClock: UserClock,
        utcClock: UtcClock
    ): DateOfBirth {
      return fromAgeOrDate(recentPatient.age, recentPatient.dateOfBirth, userClock, utcClock)
    }

    private fun fromAgeOrDate(
        age: Age?,
        date: LocalDate?,
        userClock: UserClock,
        utcClock: UtcClock
    ): DateOfBirth {
      return when {
        date != null -> fromDate(date, utcClock)
        age != null -> fromAge(age, userClock)
        else -> throw IllegalStateException("Both age AND dateOfBirth cannot be null!")
      }
    }
  }

  enum class Type {
    EXACT, FROM_AGE
  }
}
