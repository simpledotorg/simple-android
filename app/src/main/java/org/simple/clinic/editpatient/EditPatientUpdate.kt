package org.simple.clinic.editpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.editpatient.EditPatientValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.EditPatientValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.EditPatientValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.EditPatientValidationError.STATE_EMPTY
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator

class EditPatientUpdate(
    private val numberValidator: PhoneNumberValidator,
    private val dobValidator: UserInputDateValidator
) : Update<EditPatientModel, EditPatientEvent, EditPatientEffect> {
  private val errorsForEventType = mapOf(
      PhoneNumberChanged::class to setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_LONG, PHONE_NUMBER_LENGTH_TOO_SHORT),
      NameChanged::class to setOf(FULL_NAME_EMPTY),
      ColonyOrVillageChanged::class to setOf(COLONY_OR_VILLAGE_EMPTY),
      StateChanged::class to setOf(STATE_EMPTY),
      DistrictChanged::class to setOf(DISTRICT_EMPTY),
      AgeChanged::class to setOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT),
      DateOfBirthChanged::class to setOf(INVALID_DATE_OF_BIRTH, DATE_OF_BIRTH_IN_FUTURE)
  )

  override fun update(
      model: EditPatientModel,
      event: EditPatientEvent
  ): Next<EditPatientModel, EditPatientEffect> {
    return when (event) {
      is DateOfBirthFocusChanged -> dispatchDatePatternInLabelEffect(event)

      is NameChanged -> {
        next(
            model.updateName(event.name),
            setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
        )
      }

      is GenderChanged -> {
        next(model.updateGender(event.gender))
      }

      is PhoneNumberChanged -> {
        next(
            model.updatePhoneNumber(event.phoneNumber),
            setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
        )
      }

      is ColonyOrVillageChanged -> {
        next(
            model.updateColonyOrVillage(event.colonyOrVillage),
            setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
        )
      }

      is DistrictChanged -> {
        next(
            model.updateDistrict(event.district),
            setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
        )
      }

      is StateChanged -> {
        next(
            model.updateState(event.state),
            setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
        )
      }

      is AgeChanged -> {
        next(
            model.updateAge(event.age),
            setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
        )
      }

      is DateOfBirthChanged -> {
        next(
            model.updateDateOfBirth(event.dateOfBirth),
            if (event.dateOfBirth.isBlank()) {
              setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
            } else {
              setOf(ShowDatePatternInDateOfBirthLabelEffect, HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
            }
        )
      }

      is SaveClicked -> {
        val validationErrors = model.ongoingEntry.validate(model.savedPhoneNumber, numberValidator, dobValidator)
        return if (validationErrors.isEmpty()) {
          val (_, ongoingEntry, savedPatient, savedAddress, savedPhoneNumber) = model
          onlyEffect(SavePatientEffect(model.ongoingEntry, savedPatient, savedAddress, savedPhoneNumber))
        } else {
          onlyEffect(ShowValidationErrorsEffect(validationErrors))
        }
      }

      is BackClicked -> {
        if (model.savedEntry != model.ongoingEntry) onlyEffect(ShowDiscardChangesAlertEffect) else onlyEffect(GoBackEffect)
      }
    }
  }

  private fun dispatchDatePatternInLabelEffect(
      event: DateOfBirthFocusChanged
  ): Next<EditPatientModel, EditPatientEffect> {
    val showOrHideLabelEffect = if (event.hasFocus) {
      ShowDatePatternInDateOfBirthLabelEffect
    } else {
      HideDatePatternInDateOfBirthLabelEffect
    }
    return onlyEffect(showOrHideLabelEffect)
  }

  private fun onlyEffect(
      effect: EditPatientEffect
  ): Next<EditPatientModel, EditPatientEffect> {
    return dispatch<EditPatientModel, EditPatientEffect>(setOf(effect))
  }
}
