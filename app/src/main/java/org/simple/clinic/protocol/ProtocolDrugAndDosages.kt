package org.simple.clinic.protocol

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.PrescribedDrug

@Parcelize
data class ProtocolDrugAndDosages(
    val drugName: String,
    val drugs: List<ProtocolDrug>
) : Parcelable {

  fun matches(prescribedDrug: PrescribedDrug): Boolean {
    return drugs.any { protocolDrug ->
      protocolDrug.name == prescribedDrug.name && protocolDrug.dosage == prescribedDrug.dosage
    }
  }
}
