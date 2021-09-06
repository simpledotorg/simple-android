package org.simple.clinic.drugs.selection.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.DrugInfoProgressState.DONE
import org.simple.clinic.drugs.selection.custom.DrugInfoProgressState.IN_PROGRESS
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem

@Parcelize
data class CustomDrugEntryModel(
    val openAs: OpenAs,
    val drugName: String?,
    val dosage: String?,
    val dosageHasFocus: Boolean?,
    val frequency: DrugFrequency?,
    val rxNormCode: String?,
    val dosagePlaceholder: String,
    val drugFrequencyChoiceItems: List<DrugFrequencyChoiceItem>?,
    val drugFrequencyToFrequencyChoiceItemMap: Map<DrugFrequency?, DrugFrequencyChoiceItem>?,
    val drugInfoProgressState: DrugInfoProgressState?
) : Parcelable {
  companion object {
    fun default(
        openAs: OpenAs,
        dosagePlaceholder: String
    ) = CustomDrugEntryModel(
        openAs = openAs,
        drugName = null,
        dosage = null,
        dosageHasFocus = null,
        frequency = null,
        rxNormCode = null,
        dosagePlaceholder = dosagePlaceholder,
        drugFrequencyChoiceItems = null,
        drugFrequencyToFrequencyChoiceItemMap = null,
        drugInfoProgressState = null)
  }

  val isCustomDrugEntrySheetInfoLoaded: Boolean
    get() = drugInfoProgressState == DONE

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

  fun drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems: List<DrugFrequencyChoiceItem>): CustomDrugEntryModel {
    return copy(drugFrequencyChoiceItems = drugFrequencyChoiceItems)
  }

  fun drugFrequencyToFrequencyChoiceItemMapLoaded(drugFrequencyToFrequencyChoiceItemMap: Map<DrugFrequency?, DrugFrequencyChoiceItem>?): CustomDrugEntryModel {
    return copy(drugFrequencyToFrequencyChoiceItemMap = drugFrequencyToFrequencyChoiceItemMap)
  }

  fun customDrugEntryProgressStateLoaded(): CustomDrugEntryModel {
    return copy(drugInfoProgressState = DONE)
  }

  fun customDrugEntryProgressStateLoading(): CustomDrugEntryModel {
    return copy(drugInfoProgressState = IN_PROGRESS)
  }
}
