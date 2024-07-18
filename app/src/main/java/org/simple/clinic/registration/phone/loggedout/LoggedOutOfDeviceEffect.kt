package org.simple.clinic.registration.phone.loggedout

sealed class LoggedOutOfDeviceEffect

data object LogoutUser : LoggedOutOfDeviceEffect()

data class ThrowError(val cause: Throwable) : LoggedOutOfDeviceEffect()
