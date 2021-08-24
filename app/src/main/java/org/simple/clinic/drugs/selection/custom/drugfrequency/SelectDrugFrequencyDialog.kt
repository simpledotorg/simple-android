package org.simple.clinic.drugs.selection.custom.drugfrequency

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.search.DrugFrequency.BD
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.search.DrugFrequency.QDS
import org.simple.clinic.drugs.search.DrugFrequency.TDS
import org.simple.clinic.drugs.search.DrugFrequency.Unknown
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SelectDrugFrequencyDialog : DialogFragment() {
  @Inject
  lateinit var router: Router

  private val screenKey: Key by unsafeLazy { ScreenKey.key(this) }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val selectedValueIndex = frequenciesArrayIndexFromDrugFrequency(screenKey.drugFrequency)
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.custom_drug_entry_sheet_frequency))
        .setSingleChoiceItems(resources.getStringArray(R.array.custom_drug_entry_sheet_frequencies), selectedValueIndex) { _, indexSelected ->
          router.popWithResult(Succeeded(drugFrequencyFromFrequenciesArrayIndex(indexSelected)))
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

  private fun drugFrequencyFromFrequenciesArrayIndex(index: Int): DrugFrequency {
    return when (index) {
      1 -> OD
      2 -> BD
      3 -> QDS
      4 -> TDS
      else -> Unknown("None")
    }
  }

  private fun frequenciesArrayIndexFromDrugFrequency(drugFrequency: DrugFrequency?): Int {
    return when (drugFrequency) {
      OD -> 1
      BD -> 2
      QDS -> 3
      TDS -> 4
      else -> 0
    }
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
}
