package org.simple.clinic.enterotp

import java.time.Duration

data class BruteForceOtpEntryProtectionConfig(
    val limitOfFailedAttempts: Int,
    val blockDuration: Duration,
    val minOtpEntries: Int
)
