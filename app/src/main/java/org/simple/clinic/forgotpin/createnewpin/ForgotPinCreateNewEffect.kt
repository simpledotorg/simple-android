package org.simple.clinic.forgotpin.createnewpin

sealed class ForgotPinCreateNewEffect

object LoadLoggedInUser : ForgotPinCreateNewEffect()

object LoadCurrentFacility : ForgotPinCreateNewEffect()

data class ValidatePin(val pin: String?) : ForgotPinCreateNewEffect()

data class ShowConfirmPinScreen(val pin: String) : ForgotPinCreateNewEffect()

object HideInvalidPinError : ForgotPinCreateNewEffect()

sealed class ForgotPinCreateNewViewEffect : ForgotPinCreateNewEffect()

object ShowInvalidPinError : ForgotPinCreateNewViewEffect()
