package org.simple.clinic.sync.indicator

import org.threeten.bp.Duration

data class SyncIndicatorConfig(val syncFailureThreshold: Duration)
