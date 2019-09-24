package org.simple.clinic.patient

import androidx.room.Embedded
import org.simple.clinic.patient.DateOfBirth.Type.GUESSED
import org.simple.clinic.patient.DateOfBirth.Type.RECORDED
import org.simple.clinic.util.UserClock
import org.threeten.bp.LocalDate

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
    val type: Type
) {

  companion object {
    fun fromDate(date: LocalDate): DateOfBirth {
      return DateOfBirth(date, RECORDED)
    }

    fun fromAge(age: Age, userClock: UserClock): DateOfBirth {
      val ageRecordedAtDate = age.updatedAt.atZone(userClock.zone)

      val guessedDateOfBirth = ageRecordedAtDate.minusYears(age.value.toLong()).toLocalDate()

      return DateOfBirth(guessedDateOfBirth, GUESSED)
    }
  }

  enum class Type {
    RECORDED, GUESSED
  }
}
