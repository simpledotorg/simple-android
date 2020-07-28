package org.simple.clinic.forgotpin.createnewpin

sealed class ForgotPinCreateNewEffect

object LoadLoggedInUser : ForgotPinCreateNewEffect()

object LoadCurrentFacility : ForgotPinCreateNewEffect()

data class ValidatePin(val pin: String?) : ForgotPinCreateNewEffect()
