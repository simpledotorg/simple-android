package org.simple.clinic.registerorlogin

sealed class AuthenticationEffect

data object OpenCountrySelectionScreen : AuthenticationEffect()

data object OpenRegistrationPhoneScreen : AuthenticationEffect()
