package org.simple.clinic.home.report

import org.simple.clinic.util.Optional

sealed class ReportsEvent

data class ReportsLoaded(val reportsContent: Optional<String>) : ReportsEvent()

object WebBackClicked : ReportsEvent()
