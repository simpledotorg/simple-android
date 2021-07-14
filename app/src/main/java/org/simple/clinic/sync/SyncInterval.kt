package org.simple.clinic.sync

import java.time.Duration

data class SyncInterval(val frequency: Duration, val backOffDelay: Duration)
