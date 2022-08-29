package org.simple.clinic.reports

import androidx.annotation.VisibleForTesting
import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.storage.text.TextStore
import java.util.Optional
import javax.inject.Inject

@AppScope
class ReportsRepository @Inject constructor(
    private val textStore: TextStore
) {

  companion object {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val REPORTS_KEY = "reports"
  }

  fun reportsContentText(): Observable<Optional<String>> {
    return textStore.getAsStream(REPORTS_KEY)
  }

  fun updateReports(reportContent: String) {
    textStore.put(REPORTS_KEY, reportContent)
  }

  fun deleteReports() {
    return textStore.delete(REPORTS_KEY)
  }
}
