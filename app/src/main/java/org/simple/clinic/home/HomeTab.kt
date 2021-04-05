package org.simple.clinic.home

import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.home.overdue.OverdueScreenKey
import org.simple.clinic.home.patients.PatientsTabScreen
import org.simple.clinic.home.report.ReportsScreenKey
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.compat.wrap

enum class HomeTab(
    val key: ScreenKey,
    @StringRes val title: Int
) {

  PATIENTS(PatientsTabScreen.Key(), R.string.tab_patient),

  OVERDUE(OverdueScreenKey().wrap(), R.string.tab_overdue),

  REPORTS(ReportsScreenKey().wrap(), R.string.tab_progress)
}
