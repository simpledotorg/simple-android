package org.simple.clinic.bp.history

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.di.injector
import java.util.UUID

class BloodPressureHistoryScreen(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), BloodPressureHistoryScreenUi, BloodPressureHistoryScreenUiActions {

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<BloodPressureHistoryScreenInjector>().inject(this)
  }

  override fun showBloodPressureHistory(bloodPressures: List<BloodPressureMeasurement>) {
  }

  override fun openBloodPressureEntrySheet() {
  }

  override fun openBloodPressureUpdateSheet(bpUuid: UUID) {
  }
}
