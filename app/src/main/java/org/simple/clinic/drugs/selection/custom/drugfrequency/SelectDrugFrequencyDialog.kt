package org.simple.clinic.drugs.selection.custom.drugfrequency

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SelectDrugFrequencyDialog : AppCompatDialogFragment() {

  companion object {

    fun readDrugFrequency(result: Succeeded): DrugFrequency? {
      return (result.result as SelectedDrugFrequency).drugFrequency
    }
  }

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>

  private val screenKey: Key by unsafeLazy { ScreenKey.key(this) }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val drugFrequencies = drugFrequencyToLabelMap.keys.toList()
    val drugFrequencyLabels = drugFrequencyToLabelMap.values.map { it.label }
    val selectedValueIndex = drugFrequencies.indexOf(screenKey.drugFrequency)

    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.custom_drug_entry_sheet_frequency))
        .setSingleChoiceItems(drugFrequencyLabels.toTypedArray(), selectedValueIndex) { _, indexSelected ->
          router.popWithResult(Succeeded(SelectedDrugFrequency(drugFrequencies[indexSelected])))
        }
        .setPositiveButton(getString(R.string.custom_drug_entry_sheet_frequency_dialog_done)) { _, _ ->
          router.pop()
        }
        .create()
  }

  override fun onCancel(dialog: DialogInterface) {
    backPressed()
    super.onCancel(dialog)
  }

  private fun backPressed() {
    requireActivity().onBackPressed()
  }

  @Parcelize
  data class Key(
      val drugFrequency: DrugFrequency?,
      override val analyticsName: String = "Drug Frequency Dialog"
  ) : ScreenKey() {
    @IgnoredOnParcel
    override val type = ScreenType.Modal

    override fun instantiateFragment() = SelectDrugFrequencyDialog()
  }

  interface Injector {
    fun inject(target: SelectDrugFrequencyDialog)
  }

  @Parcelize
  data class SelectedDrugFrequency(val drugFrequency: DrugFrequency?) : Parcelable
}
