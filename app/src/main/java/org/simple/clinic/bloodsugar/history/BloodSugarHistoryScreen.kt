package org.simple.clinic.bloodsugar.history

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.di.injector
import org.simple.clinic.patient.Patient
import java.util.UUID

class BloodSugarHistoryScreen(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), BloodSugarHistoryScreenUi, BloodSugarHistoryScreenUiActions {

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<BloodSugarHistoryScreenInjector>().inject(this)
  }

  override fun showPatientInformation(patient: Patient) {
  }

  override fun showBloodSugarHistory(bloodSugars: List<BloodSugarMeasurement>) {
  }

  override fun openBloodSugarEntrySheet(patientUuid: UUID) {
  }
}
