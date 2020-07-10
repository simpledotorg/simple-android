package org.simple.clinic.security.pin

import java.time.Duration

data class BruteForceProtectionConfig(
    val limitOfFailedAttempts: Int,
    val blockDuration: Duration
)
