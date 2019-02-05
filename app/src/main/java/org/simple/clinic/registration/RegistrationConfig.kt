package org.simple.clinic.registration

import org.threeten.bp.Duration

data class RegistrationConfig(
    val retryBackOffDelayInMinutes: Long, // TODO: convert to Duration
    val locationListenerExpiry: Duration,
    val locationUpdateInterval: Duration
)
