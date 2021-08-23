package org.simple.clinic.contactpatient

import org.simple.clinic.contactpatient.UiMode.CallPatient
import org.simple.clinic.contactpatient.UiMode.SetAppointmentReminder
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.util.ParcelableOptional
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.daysTill
import java.time.LocalDate

class ContactPatientUiRenderer(
    private val ui: ContactPatientUi,
    private val clock: UserClock
) : ViewRenderer<ContactPatientModel> {

  override fun render(model: ContactPatientModel) {
    if (!model.isPatientContactInfoLoaded) {
      ui.showProgress()
      return
    }

    ui.hideProgress()
    when (model.uiMode) {
      CallPatient -> renderCallPatientViewBasedOnFeatureFlag(model)
      SetAppointmentReminder -> renderSetAppointmentReminderView(model)
    }
  }

  private fun renderCallPatientViewBasedOnFeatureFlag(model: ContactPatientModel) {
    if (model.overdueListChangesFeatureEnabled) {
      renderCallPatientView(model)
    } else {
      renderCallPatientView_Old(model)
    }
  }

  private fun renderSetAppointmentReminderView(model: ContactPatientModel) {
    renderSelectedAppointmentDate(model)
    toggleStateOfReminderDateSteppers(model)

    ui.switchToSetAppointmentReminderView()
  }

  private fun renderCallPatientView_Old(model: ContactPatientModel) {
    if (model.hasLoadedPatientProfile) {
      renderPatientProfile_Old(model.patientProfile!!)
    }

    if (model.hasLoadedAppointment) {
      toggleCallResultSection(model.appointment!!)
    }

    if (model.secureCallingFeatureEnabled) {
      ui.showSecureCallUi_Old()
    } else {
      ui.hideSecureCallUi_Old()
    }

    ui.switchToCallPatientView_Old()
  }

  private fun renderCallPatientView(model: ContactPatientModel) {
    if (model.hasLoadedPatientProfile) {
      renderPatientProfile(model.patientProfile!!)
    }

    if (model.hasRegisteredFacility && model.hasCurrentFacility) {
      renderPatientFacilityLabel(model.appointmentIsInRegisteredFacility)
    }

    if (model.patientProfileHasPhoneNumber && model.isAppointmentPresent.not()) {
      loadPatientWithPhoneNumber()
      loadSecureCallingUi(model)
    } else if (model.patientProfileHasPhoneNumber && model.isAppointmentPresent) {
      showPatientCallResult()
      loadSecureCallingUi(model)
    } else if (!model.patientProfileHasPhoneNumber && model.isAppointmentPresent) {
      ui.showPatientWithNoPhoneNumberUi()
      ui.hidePatientWithPhoneNumberUi()
      ui.setResultLabelText()
      ui.showPatientWithNoPhoneNumberResults()
    } else {
      ui.showPatientWithNoPhoneNumberUi()
      ui.hidePatientWithPhoneNumberUi()
      ui.setResultLabelText()
    }

    ui.switchToCallPatientView()
  }

  private fun showPatientCallResult() {
    ui.showPatientWithCallResultUi()
    ui.setResultOfCallLabelText()
    ui.showPatientWithPhoneNumberUi()
    ui.hidePatientWithNoPhoneNumberUi()
  }

  private fun loadPatientWithPhoneNumber() {
    ui.showPatientWithPhoneNumberUi()
    ui.hidePatientWithNoPhoneNumberUi()
    ui.hidePatientWithCallResultUi()
  }

  private fun renderPatientFacilityLabel(appointmentIsInRegisteredFacility: Boolean) {
    if (appointmentIsInRegisteredFacility) {
      ui.setRegisterAtLabelText()
    } else {
      ui.setTransferredFromLabelText()
    }
  }

  private fun loadSecureCallingUi(model: ContactPatientModel) {
    if (model.secureCallingFeatureEnabled) {
      ui.showSecureCallUi()
    } else {
      ui.hideSecureCallUi()
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

  private fun renderSelectedAppointmentDate(
      model: ContactPatientModel
  ) {
    val exactlyMatchingReminderPeriod = findReminderPeriodExactlyMatchingDate(model.potentialAppointments, model.selectedAppointmentDate)
    val selectedReminderPeriod = exactlyMatchingReminderPeriod
        ?: daysUntilTodayFrom(model.selectedAppointmentDate)

    ui.renderSelectedAppointmentDate(
        selectedReminderPeriod,
        model.selectedAppointmentDate
    )
  }

  private fun daysUntilTodayFrom(date: LocalDate): TimeToAppointment.Days {
    val today = LocalDate.now(clock)
    return TimeToAppointment.Days(today daysTill date)
  }

  private fun findReminderPeriodExactlyMatchingDate(
      potentialAppointmentDates: List<PotentialAppointmentDate>,
      date: LocalDate
  ): TimeToAppointment? {
    return potentialAppointmentDates.firstOrNull { it.scheduledFor == date }?.timeToAppointment
  }

  private fun toggleCallResultSection(appointment: ParcelableOptional<OverdueAppointment>) {
    if (appointment.isEmpty()) {
      ui.hideCallResultSection_Old()
    } else {
      ui.showCallResultSection_Old()
    }
  }

  private fun renderPatientProfile_Old(
      patientProfile: ContactPatientProfile
  ) {
    val patientAge = patientProfile.patient.ageDetails.estimateAge(clock)

    ui.renderPatientDetails_Old(
        name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientAge,
        phoneNumber = patientProfile.phoneNumbers.first().number
    )
  }

  private fun renderPatientProfile(
      patientProfile: ContactPatientProfile
  ) {
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
