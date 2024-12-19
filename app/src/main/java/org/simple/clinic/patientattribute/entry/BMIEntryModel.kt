package org.simple.clinic.patientattribute.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class BMIEntryModel(
    val patientUUID: UUID,
    val height: String = "",
    val weight: String = ""
) : Parcelable {
  companion object {
    fun default(patientUUID: UUID) = BMIEntryModel(
        patientUUID = patientUUID,
    )
  }
}
