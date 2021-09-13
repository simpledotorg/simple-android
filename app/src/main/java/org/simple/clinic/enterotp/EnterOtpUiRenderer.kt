package org.simple.clinic.enterotp

import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Allowed
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Blocked
import org.simple.clinic.enterotp.OtpEntryMode.BruteForceOtpEntryLocked
import org.simple.clinic.enterotp.OtpEntryMode.OtpEntry
import org.simple.clinic.enterotp.ValidationResult.IsNotRequiredLength
import org.simple.clinic.enterotp.ValidationResult.NotValidated
import org.simple.clinic.enterotp.ValidationResult.Valid
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class EnterOtpUiRenderer(
    private val ui: EnterOtpUi
) : ViewRenderer<EnterOtpModel> {

  private val phoneNumberChangedCallback = ValueChangedCallback<String>()

  private val isAsyncOperationOngoingChangedCallback = ValueChangedCallback<Boolean>()

  override fun render(model: EnterOtpModel) {
    if (model.hasLoadedUser) {
      phoneNumberChangedCallback.pass(model.user!!.phoneNumber, ui::showUserPhoneNumber)
    }

    when (model.otpValidationResult) {
      NotValidated -> { /* Nothing to do here */
      }
      IsNotRequiredLength -> ui.showIncorrectOtpError()
      Valid -> { /* Nothing to do here */
      }
    }

    isAsyncOperationOngoingChangedCallback.pass(model.isAsyncOperationOngoing) { isAsyncOperationOngoing ->
      if (isAsyncOperationOngoing)
        ui.showProgress()
      else
        ui.hideProgress()
    }

    when (model.protectedState) {
      is Allowed -> {
        ui.showOtpEntryMode(OtpEntry)
        generateUiForAllowingOtpEntry(model.hasNoIncorrectPinEntries, model.protectedState.attemptsMade, model.protectedState.attemptsRemaining)
      }
      is Blocked -> {
        ui.showOtpEntryMode(BruteForceOtpEntryLocked(model.protectedState.blockedTill))
        ui.showLimitReachedError(model.protectedState.attemptsMade)
      }
    }
  }

  private fun generateUiForAllowingOtpEntry(hasNoIncorrectPinEntries: Boolean, attemptsMade: Int, attemptsRemaining: Int) {
    if (hasNoIncorrectPinEntries) {
      ui.hideError()
    } else {
      ui.showFailedAttemptOtpError(attemptsMade = attemptsMade, attemptsRemaining = attemptsRemaining)
    }
  }
}
