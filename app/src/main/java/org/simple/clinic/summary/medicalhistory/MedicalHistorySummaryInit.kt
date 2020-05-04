package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class MedicalHistorySummaryInit : Init<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {

  override fun init(model: MedicalHistorySummaryModel): First<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    return first(model, LoadMedicalHistory(model.patientUuid))
  }
}
