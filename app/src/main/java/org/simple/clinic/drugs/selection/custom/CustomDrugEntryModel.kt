package org.simple.clinic.drugs.selection.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.search.DrugFrequency

@Parcelize
data class CustomDrugEntryModel(
    val openAs: OpenAs,
    val drugName: String?,
    val dosage: String?,
    val dosageHasFocus: Boolean?,
    val frequency: DrugFrequency?,
    val rxNormCode: String?
) : Parcelable {
  companion object {
    fun default(
        openAs: OpenAs
    ) = CustomDrugEntryModel(
        openAs = openAs,
        drugName = null,
        dosage = null,
        dosageHasFocus = null,
        frequency = null,
        rxNormCode = null)
  }

  fun dosageEdited(dosage: String?): CustomDrugEntryModel {
    return copy(dosage = dosage)
  }

  fun dosageFocusChanged(hasFocus: Boolean): CustomDrugEntryModel {
    return copy(dosageHasFocus = hasFocus)
  }

  fun frequencyEdited(frequency: DrugFrequency?): CustomDrugEntryModel {
    return copy(frequency = frequency)
  }

  fun drugNameLoaded(drugName: String): CustomDrugEntryModel {
    return copy(drugName = drugName)
  }

  fun rxNormCodeEdited(rxNormCode: String?): CustomDrugEntryModel {
    return copy(rxNormCode = rxNormCode)
  }
}
