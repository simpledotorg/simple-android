package org.simple.clinic.home

import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.home.overdue.OverdueScreenKey
import org.simple.clinic.home.patients.PatientsTabScreenKey
import org.simple.clinic.home.report.ReportsScreenKey
import org.simple.clinic.router.screen.FullScreenKey

enum class HomeTab(
    val key: FullScreenKey,
    @StringRes val title: Int
) {

  PATIENTS(PatientsTabScreenKey(), R.string.tab_patient),

  OVERDUE(OverdueScreenKey(), R.string.tab_overdue),

  REPORTS(ReportsScreenKey(), R.string.tab_progress)
}
