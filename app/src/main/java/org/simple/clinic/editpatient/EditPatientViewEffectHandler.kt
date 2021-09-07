package org.simple.clinic.editpatient

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.util.UserClock

class EditPatientViewEffectHandler @AssistedInject constructor(
    private val userClock: UserClock,
    @Assisted private val ui: EditPatientUi
) : ViewEffectsHandler<EditPatientViewEffect> {

  @AssistedFactory
  interface Factory {
    fun create(ui: EditPatientUi): EditPatientViewEffectHandler
  }

  override fun handle(viewEffect: EditPatientViewEffect) {
    when (viewEffect) {
      is PrefillFormEffect -> prefillFormFields(viewEffect)
      is HideValidationErrorsEffect -> ui.hideValidationErrors(viewEffect.validationErrors)
      is SetupUi -> ui.setupUi(viewEffect.inputFields)
      is DisplayBpPassportsEffect -> displayBpPassports(viewEffect.bpPassports)
      is ShowValidationErrorsEffect -> showValidationErrors(viewEffect.validationErrors)
      ShowDatePatternInDateOfBirthLabelEffect -> ui.showDatePatternInDateOfBirthLabel()
      HideDatePatternInDateOfBirthLabelEffect -> ui.hideDatePatternInDateOfBirthLabel()
      GoBackEffect -> ui.goBack()
      ShowDiscardChangesAlertEffect -> ui.showDiscardChangesAlert()
    }
  }

  private fun showValidationErrors(validationErrors: Set<EditPatientValidationError>) {
    with(ui) {
      showValidationErrors(validationErrors)
      scrollToFirstFieldWithError()
    }
  }

  private fun displayBpPassports(bpPassports: List<BusinessId>) {
    val identifiers = bpPassports.map { it.identifier.displayValue() }
    ui.displayBpPassports(identifiers)
  }

  private fun prefillFormFields(prefillFormFieldsEffect: PrefillFormEffect) {
    val (patient, address, phoneNumber, alternateId) = prefillFormFieldsEffect

    with(ui) {
      setPatientName(patient.fullName)
      setGender(patient.gender)
      setState(address.state)
      setDistrict(address.district)
      setStreetAddress(address.streetAddress)
      setZone(address.zone)

      if (address.colonyOrVillage.isNullOrBlank().not()) {
        setColonyOrVillage(address.colonyOrVillage!!)
      }

      if (phoneNumber != null) {
        setPatientPhoneNumber(phoneNumber.number)
      }

      if (alternateId != null) {
        setAlternateId(alternateId.identifier)
      }
    }

    val dateOfBirth = patient.ageDetails
    when (dateOfBirth.type) {
      PatientAgeDetails.Type.EXACT -> ui.setPatientDateOfBirth(dateOfBirth.approximateDateOfBirth(userClock))
      PatientAgeDetails.Type.FROM_AGE -> ui.setPatientAge(dateOfBirth.estimateAge(userClock))
    }
  }
}
