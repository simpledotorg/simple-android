package org.simple.clinic.summary.bloodpressures.newbpsummary.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUi
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUiActions
import java.util.UUID

class NewBloodPressureSummaryView(
    context: Context,
    attrs: AttributeSet
) : CardView(context, attrs), NewBloodPressureSummaryViewUi, NewBloodPressureSummaryViewUiActions {

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
  }

  override fun showNoBloodPressuresView() {
  }

  override fun showBloodPressures(bloodPressures: List<BloodPressureMeasurement>) {
  }

  override fun showSeeAllButton() {
  }

  override fun hideSeeAllButton() {
  }

  override fun openBloodPressureEntrySheet(patientUuid: UUID) {
  }

  override fun openBloodPressureUpdateSheet(bpUuid: UUID) {
  }

  override fun showBloodPressureHistoryScreen(patientUuid: UUID) {
  }
}
