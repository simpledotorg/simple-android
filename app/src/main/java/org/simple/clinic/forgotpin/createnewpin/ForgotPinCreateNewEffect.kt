package org.simple.clinic.forgotpin.createnewpin

sealed class ForgotPinCreateNewEffect

data object LoadLoggedInUser : ForgotPinCreateNewEffect()

data object LoadCurrentFacility : ForgotPinCreateNewEffect()

data class ValidatePin(val pin: String?) : ForgotPinCreateNewEffect()

sealed class ForgotPinCreateNewViewEffect : ForgotPinCreateNewEffect()

data object ShowInvalidPinError : ForgotPinCreateNewViewEffect()

data class ShowConfirmPinScreen(val pin: String) : ForgotPinCreateNewViewEffect()

data object HideInvalidPinError : ForgotPinCreateNewViewEffect()
