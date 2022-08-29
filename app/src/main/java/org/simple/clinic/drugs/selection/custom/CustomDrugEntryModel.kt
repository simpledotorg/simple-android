package org.simple.clinic.drugs.selection.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.ButtonState.SAVING
import org.simple.clinic.drugs.selection.custom.DrugInfoProgressState.DONE
import org.simple.clinic.drugs.selection.custom.DrugInfoProgressState.IN_PROGRESS
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel

@Parcelize
data class CustomDrugEntryModel(
    val openAs: OpenAs,
    val drugName: String?,
    val dosage: String?,
    val dosageHasFocus: Boolean?,
    val frequency: DrugFrequency?,
    val rxNormCode: String?,
    val dosagePlaceholder: String,
    val drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>?,
    val drugInfoProgressState: DrugInfoProgressState?,
    val saveButtonState: ButtonState?
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
        drugFrequencyToLabelMap = null,
        drugInfoProgressState = null,
        saveButtonState = null)
  }

  val isCustomDrugEntrySheetInfoLoaded: Boolean
    get() = drugInfoProgressState == DONE

  val isSaveButtonInProgressState: Boolean
    get() = saveButtonState == SAVING

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

  fun drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>?): CustomDrugEntryModel {
    return copy(drugFrequencyToLabelMap = drugFrequencyToLabelMap)
  }

  fun drugInfoProgressStateLoaded(): CustomDrugEntryModel {
    return copy(drugInfoProgressState = DONE)
  }

  fun drugInfoProgressStateLoading(): CustomDrugEntryModel {
    return copy(drugInfoProgressState = IN_PROGRESS)
  }

  fun saveButtonStateChanged(saveButtonState: ButtonState): CustomDrugEntryModel {
    return copy(saveButtonState = saveButtonState)
  }
}
