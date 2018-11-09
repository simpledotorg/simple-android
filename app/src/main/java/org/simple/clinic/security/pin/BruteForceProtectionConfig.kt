package org.simple.clinic.security.pin

import org.threeten.bp.Duration

data class BruteForceProtectionConfig(
    val isEnabled: Boolean,
    val limitOfFailedAttempts: Int,
    val blockDuration: Duration
)
