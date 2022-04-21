package org.simple.clinic.forgotpin.confirmpin

sealed class ForgotPinConfirmPinEffect

object LoadLoggedInUser : ForgotPinConfirmPinEffect()

object LoadCurrentFacility : ForgotPinConfirmPinEffect()

data class ValidatePinConfirmation(
    val previousPin: String,
    val enteredPin: String
) : ForgotPinConfirmPinEffect()

object ShowProgress : ForgotPinConfirmPinEffect()

object ShowNetworkError : ForgotPinConfirmPinEffect()

object ShowUnexpectedError : ForgotPinConfirmPinEffect()

object GoToHomeScreen : ForgotPinConfirmPinEffect()

data class SyncPatientDataAndResetPin(val newPin: String) : ForgotPinConfirmPinEffect()

sealed class ForgotPinConfirmPinViewEffect : ForgotPinConfirmPinEffect()

object HideError : ForgotPinConfirmPinViewEffect()

object ShowMismatchedError : ForgotPinConfirmPinViewEffect()
