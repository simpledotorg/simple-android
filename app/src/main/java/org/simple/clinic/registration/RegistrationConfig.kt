package org.simple.clinic.registration

data class RegistrationConfig constructor(
    val isRegistrationEnabled: Boolean,
    val retryBackOffDelayInMinutes: Long
)
