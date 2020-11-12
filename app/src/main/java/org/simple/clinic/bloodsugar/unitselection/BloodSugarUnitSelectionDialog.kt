package org.simple.clinic.bloodsugar.unitselection

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R

class BloodSugarUnitSelectionDialog : AppCompatDialogFragment() {

  private lateinit var layout: View

  companion object {

    private const val FRAGMENT_TAG = "blood_sugar_unit_selection_tag"

    fun show(fragmentManager: FragmentManager) {
      val fragment = BloodSugarUnitSelectionDialog()
      fragment.show(fragmentManager, FRAGMENT_TAG)
    }
  }

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    layout = LayoutInflater.from(context).inflate(R.layout.dialog_bloodsugar_selectionunit, null)
    return AlertDialog.Builder(requireContext())
        .setTitle(R.string.blood_sugar_unit_selection_choose)
        .setView(layout)
        .setPositiveButton(R.string.blood_sugar_unit_selection_done, null)
        .create()
  }
}
