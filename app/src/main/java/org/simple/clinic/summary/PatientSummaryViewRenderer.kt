package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.toLocalDateAtZone
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PatientSummaryViewRenderer(
    private val ui: PatientSummaryScreenUi,
    private val modelUpdateCallback: PatientSummaryModelUpdateCallback,
    private val userClock: UserClock,
    private val cdssOverdueLimit: Int
) : ViewRenderer<PatientSummaryModel> {

  private val clinicalDecisionSupportCallback = ValueChangedCallback<Boolean>()
  private val today = LocalDate.now(userClock)

  override fun render(model: PatientSummaryModel) {
    modelUpdateCallback.invoke(model)

    with(ui) {
      if (model.hasLoadedPatientSummaryProfile) {
        populatePatientProfile(model.patientSummaryProfile!!)
        setupUiForAssignedFacility(model)
        renderPatientDiedStatus(model)
        renderPatientSummaryToolbar(model.patientSummaryProfile)
      }

      if (model.hasLoadedCurrentFacility) {
        setupUiForDiabetesManagement(model.isDiabetesManagementEnabled)
        setupUiForTeleconsult(model)
      }

      renderNextAppointmentCard(model)

      val patientRegistrationDate = model.patientSummaryProfile?.patient?.createdAt?.toLocalDateAtZone(userClock.zone)
      if (patientRegistrationDate?.isBefore(today) == true) {
        renderClinicalDecisionBasedOnAppointment(model)
      } else {
        ui.hideClinicalDecisionSupportAlertWithoutAnimation()
      }

      renderStatinAlert(model)
    }
  }

  private fun renderClinicalDecisionBasedOnAppointment(model: PatientSummaryModel) {
    if (model.statinInfo?.canShowStatinNudge == true)
      return

    if (model.hasScheduledAppointment) {
      renderClinicalDecisionBasedOnAppointmentOverdue(model)
    } else {
      renderClinicalDecisionSupportAlert(model)
    }
  }

  private fun renderClinicalDecisionBasedOnAppointmentOverdue(model: PatientSummaryModel) {
    val appointmentScheduleDate = model.scheduledAppointment!!.get().scheduledDate
    val daysAppointmentOverdueFor = appointmentScheduleDate.until(today, ChronoUnit.DAYS)

    if (daysAppointmentOverdueFor <= cdssOverdueLimit) {
      renderClinicalDecisionSupportAlert(model)
    } else {
      ui.hideClinicalDecisionSupportAlertWithoutAnimation()
    }
  }

  private fun renderClinicalDecisionSupportAlert(model: PatientSummaryModel) {
    val canShowClinicalDecisionSupportAlert = model.hasPatientRegistrationData == true && model.readyToRender()

    if (!canShowClinicalDecisionSupportAlert) {
      ui.hideClinicalDecisionSupportAlertWithoutAnimation()
      return
    }

    clinicalDecisionSupportCallback.pass(
        model.isNewestBpEntryHigh == true &&
            model.hasPrescribedDrugsChangedToday == false
    ) { showCdssAlert ->
      if (showCdssAlert) {
        ui.showClinicalDecisionSupportAlert()
      } else {
        ui.hideClinicalDecisionSupportAlert()
      }
    }
  }

  private fun renderNextAppointmentCard(model: PatientSummaryModel) {
    if (model.hasPatientRegistrationData == true) {
      ui.showNextAppointmentCard()
    } else {
      ui.hideNextAppointmentCard()
    }
  }

  private fun renderPatientDiedStatus(model: PatientSummaryModel) {
    if (model.hasPatientDied) {
      ui.showPatientDiedStatus()
    } else {
      ui.hidePatientDiedStatus()
    }
  }

  private fun setupUiForAssignedFacility(model: PatientSummaryModel) {
    if (model.hasAssignedFacility) {
      ui.showAssignedFacilityView()
    } else {
      ui.hideAssignedFacilityView()
    }
  }

  private fun setupUiForTeleconsult(model: PatientSummaryModel) {
    if (model.openIntention is OpenIntention.ViewExistingPatientWithTeleconsultLog) {
      renderMedicalOfficerView()
    } else {
      renderUserView(model)
    }
  }

  private fun renderMedicalOfficerView() {
    ui.hideTeleconsultButton()
    ui.hideDoneButton()
    ui.showTeleconsultLogButton()
  }

  private fun renderUserView(model: PatientSummaryModel) {
    if (model.isTeleconsultationEnabled && model.isUserLoggedIn) {
      renderTeleconsultButton(model)
    } else {
      ui.hideTeleconsultButton()
    }
  }

  private fun renderTeleconsultButton(model: PatientSummaryModel) {
    if (model.hasMedicalOfficers) {
      ui.showTeleconsultButton()
    } else {
      ui.hideTeleconsultButton()
    }
  }

  private fun setupUiForDiabetesManagement(isDiabetesManagementEnabled: Boolean) {
    if (isDiabetesManagementEnabled) {
      ui.showDiabetesView()
    } else {
      ui.hideDiabetesView()
    }
  }

  private fun renderStatinAlert(model: PatientSummaryModel) {
    if (model.hasStatinInfoLoaded.not()) return
    ui.updateStatinAlert(model.statinInfo!!)

    if (model.statinInfo.canShowStatinNudge) {
      ui.hideClinicalDecisionSupportAlertWithoutAnimation()
    }
  }
}
