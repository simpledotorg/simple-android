package org.simple.clinic.bloodsugar.selection.type

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.sheet_blood_sugar_type_picker.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.BottomSheetActivity

class BloodSugarTypePickerSheet : BottomSheetActivity() {

  private val bloodSugarTypesList by unsafeLazy {
    listOf(
        BloodSugarTypeListItem(getString(R.string.bloodsugartype_rbs), Random),
        BloodSugarTypeListItem(getString(R.string.bloodsugartype_fbs), Fasting),
        BloodSugarTypeListItem(getString(R.string.bloodsugartype_ppbs), PostPrandial),
        BloodSugarTypeListItem(getString(R.string.bloodsugartype_hba1c), HbA1c)
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_blood_sugar_type_picker)
    val adapter = BloodSugarTypeAdapter { type ->
      val intent = Intent()
      intent.putExtra(EXTRA_BLOOD_SUGAR_TYPE, type)
      setResult(Activity.RESULT_OK, intent)
      finish()
    }

    typesRecyclerView.adapter = adapter
    adapter.submitList(bloodSugarTypesList)
  }

  companion object {

    const val EXTRA_BLOOD_SUGAR_TYPE = "blood_sugar_type"

    fun intent(context: Context): Intent {
      return Intent(context, BloodSugarTypePickerSheet::class.java)
    }

    fun selectedBloodSugarType(data: Intent): BloodSugarMeasurementType {
      return data.getParcelableExtra(EXTRA_BLOOD_SUGAR_TYPE) as BloodSugarMeasurementType
    }
  }
}
