package org.simple.clinic.forgotpin.confirmpin

sealed class ForgotPinConfirmPinEffect

object LoadLoggedInUser : ForgotPinConfirmPinEffect()

object LoadCurrentFacility : ForgotPinConfirmPinEffect()

object HideError : ForgotPinConfirmPinEffect()

data class ValidatePinConfirmation(val previousPin: String, val enteredPin: String) : ForgotPinConfirmPinEffect()

object ShowMismatchedError : ForgotPinConfirmPinEffect()

object ShowProgress : ForgotPinConfirmPinEffect()
