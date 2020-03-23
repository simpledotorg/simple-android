package org.simple.clinic.security.pin

sealed class PinEntryEffect

data class ValidateEnteredPin(val enteredPin: String, val pinDigest: String): PinEntryEffect()
