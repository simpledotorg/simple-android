package org.simple.clinic.home

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.home.overdue.OverdueScreenKey
import org.simple.clinic.home.patients.PatientsTabScreenKey
import org.simple.clinic.home.report.ReportsScreenKey

enum class HomeTab(@LayoutRes val key: Int, @StringRes val title: Int) {

  PATIENTS(PatientsTabScreenKey().layoutRes(), R.string.tab_patient),

  OVERDUE(OverdueScreenKey().layoutRes(), R.string.tab_overdue),

  REPORTS(ReportsScreenKey().layoutRes(), R.string.tab_progress)
}
