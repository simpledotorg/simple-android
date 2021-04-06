package org.simple.clinic.home

import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.home.overdue.OverdueScreen
import org.simple.clinic.home.patients.PatientsTabScreen
import org.simple.clinic.home.report.ReportsScreen
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.compat.wrap

enum class HomeTab(
    val key: ScreenKey,
    @StringRes val title: Int
) {

  PATIENTS(PatientsTabScreen.Key(), R.string.tab_patient),

  OVERDUE(OverdueScreen.Key(), R.string.tab_overdue),

  REPORTS(ReportsScreen.Key(), R.string.tab_progress)
}
