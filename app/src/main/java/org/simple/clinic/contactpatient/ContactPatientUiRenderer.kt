package org.simple.clinic.contactpatient

import org.simple.clinic.contactpatient.UiMode.CallPatient
import org.simple.clinic.contactpatient.UiMode.SetAppointmentReminder
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.Outcome.AgreedToVisit
import org.simple.clinic.overdue.callresult.Outcome.RemindToCallLater
import org.simple.clinic.overdue.callresult.Outcome.RemovedFromOverdueList
import org.simple.clinic.overdue.callresult.Outcome.Unknown
import org.simple.clinic.overdue.displayTextRes
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.daysTill
import org.simple.clinic.util.toLocalDateAtZone
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ContactPatientUiRenderer(
    private val ui: ContactPatientUi,
    private val clock: UserClock
) : ViewRenderer<ContactPatientModel> {

  override fun render(model: ContactPatientModel) {
    if (!model.isPatientContactInfoLoaded) {
      ui.showProgress()
    } else {
      ui.hideProgress()
      renderContactPatientUi(model)
      renderCallResultOutcome(model)
    }
  }

  private fun renderContactPatientUi(model: ContactPatientModel) {
    when (model.uiMode) {
      CallPatient -> renderCallPatientView(model)
      SetAppointmentReminder -> renderSetAppointmentReminderView(model)
    }
  }

  private fun renderCallResultOutcome(model: ContactPatientModel) {
    if (model.hasCallResult) {
      val callResult = model.callResult!!.get()
      ui.showCallResult()
      setupCallResultOutcomeUI(model, callResult)
      ui.setCallResultUpdatedAtDate(callResult.timestamps.updatedAt.toLocalDateAtZone(clock.zone))
    } else {
      ui.hideCallResult()
    }
  }

  private fun setupCallResultOutcomeUI(model: ContactPatientModel, callResult: CallResult) {
    when (callResult.outcome) {
      AgreedToVisit -> ui.setupAgreedToVisitCallResultOutcome()
      RemindToCallLater -> setupRemindToCallLaterCallResultOutcome(model.appointment.remindOn)
      RemovedFromOverdueList -> ui.setupRemovedFromListCallResultOutcome(callResult.removeReason!!.displayTextRes)
      is Unknown -> {}
    }
  }

  private fun setupRemindToCallLaterCallResultOutcome(remindOn: LocalDate?) {
    if (remindOn != null) {
      val duration = LocalDate.now(clock).until(remindOn, ChronoUnit.DAYS)
      ui.setupRemindToCallLaterCallResultOutcome(duration.toInt())
    }
  }

  private fun renderSetAppointmentReminderView(model: ContactPatientModel) {
    renderSelectedAppointmentDate(model)
    toggleStateOfReminderDateSteppers(model)

    ui.switchToSetAppointmentReminderView()
  }

  private fun renderCallPatientView(model: ContactPatientModel) {
    if (model.hasLoadedPatientProfile) {
      renderPatientProfile(model.patientProfile!!)
    }

    if (model.hasRegisteredFacility && model.hasCurrentFacility) {
      renderPatientFacilityLabel(model.patientIsAtRegisteredFacility)
    }

    renderPhoneNumberAndCallResults(model)

    ui.switchToCallPatientView()
  }

  private fun renderPhoneNumberAndCallResults(model: ContactPatientModel) {
    when {
      model.patientProfileHasPhoneNumber && model.hasPatientDied -> renderDeadPatientWithPhoneNumber(model.secureCallingFeatureEnabled)
      model.patientProfileHasPhoneNumber && !model.isOverdueAppointmentPresent -> renderPatientWithPhoneNumberAndNoAppointment(model.secureCallingFeatureEnabled)
      model.patientProfileHasPhoneNumber && model.isOverdueAppointmentPresent -> renderPatientWithPhoneNumberAndAppointment(model.secureCallingFeatureEnabled)
      !model.patientProfileHasPhoneNumber && model.isOverdueAppointmentPresent -> renderPatientWithNoPhoneNumberAndWithAppointment()
      else -> renderPatientWithoutPhoneNumberAndAppointment()
    }
  }

  private fun renderDeadPatientWithPhoneNumber(isSecureCallingEnabled: Boolean) {
    ui.showPatientWithPhoneNumberUi()
    ui.hidePatientWithNoPhoneNumberUi()
    ui.hidePatientWithPhoneNumberCallResults()
    ui.showDeadPatientStatus()
    renderSecureCalling(isSecureCallingEnabled)
  }

  private fun renderPatientWithoutPhoneNumberAndAppointment() {
    ui.showPatientWithNoPhoneNumberUi()
    ui.hidePatientWithPhoneNumberUi()
    ui.setResultLabelText()
    ui.hideDeadPatientStatus()
  }

  private fun renderPatientWithNoPhoneNumberAndWithAppointment() {
    ui.showPatientWithNoPhoneNumberUi()
    ui.hidePatientWithPhoneNumberUi()
    ui.showPatientWithNoPhoneNumberResults()
    ui.setResultLabelText()
    ui.hideDeadPatientStatus()
  }

  private fun renderPatientWithPhoneNumberAndAppointment(isSecureCallingEnabled: Boolean) {
    ui.showPatientWithPhoneNumberUi()
    ui.showPatientWithPhoneNumberCallResults()
    ui.setResultOfCallLabelText()
    ui.hidePatientWithNoPhoneNumberUi()
    ui.hideDeadPatientStatus()
    renderSecureCalling(isSecureCallingEnabled)
  }

  private fun renderPatientWithPhoneNumberAndNoAppointment(isSecureCallingEnabled: Boolean) {
    ui.showPatientWithPhoneNumberUi()
    ui.hidePatientWithNoPhoneNumberUi()
    ui.hidePatientWithPhoneNumberCallResults()
    ui.hideDeadPatientStatus()
    renderSecureCalling(isSecureCallingEnabled)
  }

  private fun renderSecureCalling(isSecureCallingEnabled: Boolean) {
    if (isSecureCallingEnabled) {
      ui.showSecureCallUi()
      ui.showNormalCallButtonText()
    } else {
      ui.hideSecureCallUi()
      ui.showCallButtonText()
    }
  }

  private fun renderPatientFacilityLabel(appointmentIsInRegisteredFacility: Boolean) {
    if (appointmentIsInRegisteredFacility) {
      ui.setRegisterAtLabelText()
    } else {
      ui.setTransferredFromLabelText()
    }
  }

  private fun toggleStateOfReminderDateSteppers(model: ContactPatientModel) {
    val earliestAvailableReminderDate = model.potentialAppointments.first().scheduledFor
    val latestAvailableReminderDate = model.potentialAppointments.last().scheduledFor

    if (model.selectedAppointmentDate == earliestAvailableReminderDate) {
      ui.disablePreviousReminderDateStepper()
    } else {
      ui.enablePreviousReminderDateStepper()
    }

    if (model.selectedAppointmentDate == latestAvailableReminderDate) {
      ui.disableNextReminderDateStepper()
    } else {
      ui.enableNextReminderDateStepper()
    }
  }

  private fun renderSelectedAppointmentDate(model: ContactPatientModel) {
    val exactlyMatchingReminderPeriod = timeToAppointmentForDate(model.potentialAppointments, model.selectedAppointmentDate)
    val selectedAppointmentReminderPeriod = exactlyMatchingReminderPeriod ?: daysUntilTodayFrom(model.selectedAppointmentDate)

    ui.renderSelectedAppointmentDate(selectedAppointmentReminderPeriod, model.selectedAppointmentDate)
  }

  private fun daysUntilTodayFrom(date: LocalDate): TimeToAppointment.Days {
    val today = LocalDate.now(clock)
    return TimeToAppointment.Days(today daysTill date)
  }

  private fun timeToAppointmentForDate(
      potentialAppointmentDates: List<PotentialAppointmentDate>,
      date: LocalDate
  ): TimeToAppointment? {
    return potentialAppointmentDates.firstOrNull { it.scheduledFor == date }?.timeToAppointment
  }

  private fun renderPatientProfile(patientProfile: ContactPatientProfile) {
    val patientAge = patientProfile.patient.ageDetails.estimateAge(clock)

    ui.renderPatientDetails(PatientDetails(
        name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientAge,
        phoneNumber = patientProfile.phoneNumbers.firstOrNull()?.number,
        patientAddress = patientAddressText(patientProfile.address),
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen
    ))
  }

  private fun patientAddressText(patientAddress: PatientAddress) = when {
    !patientAddress.streetAddress.isNullOrBlank() && !patientAddress.colonyOrVillage.isNullOrBlank() -> {
      "${patientAddress.streetAddress}, ${patientAddress.colonyOrVillage}"
    }
    !patientAddress.streetAddress.isNullOrBlank() -> patientAddress.streetAddress
    !patientAddress.colonyOrVillage.isNullOrBlank() -> patientAddress.colonyOrVillage
    else -> "${patientAddress.district}, ${patientAddress.state}"
  }
}
