package org.simple.clinic.bloodsugar.selection.type

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.sheet_blood_sugar_type_picker.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.BottomSheetActivity

class BloodSugarTypePickerSheet : BottomSheetActivity() {

  private val bloodSugarTypesList by unsafeLazy {
    listOf(
        BloodSugarTypeListItem(getString(R.string.bloodsugartype_rbs), Random),
        BloodSugarTypeListItem(getString(R.string.bloodsugartype_fbs), Fasting),
        BloodSugarTypeListItem(getString(R.string.bloodsugartype_ppbs), PostPrandial)
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_blood_sugar_type_picker)
    val adapter = BloodSugarTypeAdapter {
      // [WIP] BloodSugarEntrySheet
      TODO("BloodSugarEntrySheet not implemented yet")
    }

    typesRecyclerView.adapter = adapter
    adapter.submitList(bloodSugarTypesList)
  }

  companion object {

    fun intent(context: Context): Intent {
      return Intent(context, BloodSugarTypePickerSheet::class.java)
    }
  }
}
