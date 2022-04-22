package org.simple.clinic.forgotpin.confirmpin

sealed class ForgotPinConfirmPinEffect

object LoadLoggedInUser : ForgotPinConfirmPinEffect()

object LoadCurrentFacility : ForgotPinConfirmPinEffect()

data class ValidatePinConfirmation(
    val previousPin: String,
    val enteredPin: String
) : ForgotPinConfirmPinEffect()

data class SyncPatientDataAndResetPin(val newPin: String) : ForgotPinConfirmPinEffect()

sealed class ForgotPinConfirmPinViewEffect : ForgotPinConfirmPinEffect()

object HideError : ForgotPinConfirmPinViewEffect()

object ShowMismatchedError : ForgotPinConfirmPinViewEffect()

object ShowProgress : ForgotPinConfirmPinViewEffect()

object ShowNetworkError : ForgotPinConfirmPinViewEffect()

object ShowUnexpectedError : ForgotPinConfirmPinViewEffect()

object GoToHomeScreen : ForgotPinConfirmPinViewEffect()
