package org.simple.clinic.editpatient

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.patient.Patient

class EditPatientInit(
    private val patient: Patient,
    private val isVillageTypeAheadEnabled: Boolean
) : Init<EditPatientModel, EditPatientEffect> {
  override fun init(model: EditPatientModel): First<EditPatientModel, EditPatientEffect> {
    val effects = mutableSetOf(
        FetchBpPassportsEffect(patient.uuid),
        LoadInputFields,
    )

    if (!model.hasColonyOrVillagesList && isVillageTypeAheadEnabled) {
      effects.add(FetchColonyOrVillagesEffect)
    }

    return first(model, effects)
  }
}
