package org.simple.clinic.security.pin

import org.threeten.bp.Duration

data class BruteForceProtectionConfig(
    val limitOfFailedAttempts: Int,
    val blockDuration: Duration
)
