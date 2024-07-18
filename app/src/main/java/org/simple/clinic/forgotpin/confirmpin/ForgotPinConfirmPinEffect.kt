package org.simple.clinic.forgotpin.confirmpin

sealed class ForgotPinConfirmPinEffect

data object LoadLoggedInUser : ForgotPinConfirmPinEffect()

data object LoadCurrentFacility : ForgotPinConfirmPinEffect()

data class ValidatePinConfirmation(
    val previousPin: String,
    val enteredPin: String
) : ForgotPinConfirmPinEffect()

data class SyncPatientDataAndResetPin(val newPin: String) : ForgotPinConfirmPinEffect()

sealed class ForgotPinConfirmPinViewEffect : ForgotPinConfirmPinEffect()

data object HideError : ForgotPinConfirmPinViewEffect()

data object ShowMismatchedError : ForgotPinConfirmPinViewEffect()

data object ShowProgress : ForgotPinConfirmPinViewEffect()

data object ShowNetworkError : ForgotPinConfirmPinViewEffect()

data object ShowUnexpectedError : ForgotPinConfirmPinViewEffect()

data object GoToHomeScreen : ForgotPinConfirmPinViewEffect()
