package org.simple.clinic.bloodsugar.selection.type

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.sheet_blood_sugar_type_picker.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.selection.type.di.BloodSugarTypePickerSheetComponent
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import java.util.Locale
import javax.inject.Inject

class BloodSugarTypePickerSheet : BottomSheetActivity() {

  @Inject
  lateinit var locale: Locale

  private lateinit var component: BloodSugarTypePickerSheetComponent

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

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .bloodSugarTypePickerSheetComponentBuilder()
        .activity(this)
        .build()

    component.inject(this)
  }

  companion object {

    const val EXTRA_BLOOD_SUGAR_TYPE = "blood_sugar_type"

    fun intent(context: Context): Intent {
      return Intent(context, BloodSugarTypePickerSheet::class.java)
    }

    fun selectedBloodSugarType(data: Intent): BloodSugarMeasurementType {
      return data.getParcelableExtra(EXTRA_BLOOD_SUGAR_TYPE)!!
    }
  }
}
