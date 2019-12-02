package org.simple.clinic.summary.medicalhistory

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import org.simple.clinic.R

class MedicalHistorySummaryView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_medicalhistoryview_content, this, true)
  }
}
