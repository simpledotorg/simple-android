package org.simple.clinic.drugs.selection.dosage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.sheet_dosage_picker.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.drugs.selection.dosage.di.DosagePickerSheetComponent
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class DosagePickerSheet : BottomSheetActivity() {

  @Inject
  lateinit var controller: DosagePickerSheetController

  @Inject
  lateinit var locale: Locale

  private lateinit var component: DosagePickerSheetComponent

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private val adapter = ItemAdapter(DosageDiffer())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_dosage_picker)

    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)
    displayDrugName()

    bindUiToController(
        ui = this,
        events = Observable.merge(sheetCreates(), adapter.itemEvents),
        controller = controller,
        screenDestroys = onDestroys
    )
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
        .dosagePickerSheetComponentBuilder()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME)
    val patientUuid = intent.getSerializableExtra(KEY_PATIENT_UUID) as UUID
    val prescribedDrugUuid = intent.getSerializableExtra(KEY_PRESCRIBED_DRUG_UUID).toOptional() as Optional<UUID>
    return Observable.just(DosagePickerSheetCreated(drugName, patientUuid, prescribedDrugUuid))
  }

  private fun displayDrugName() {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME)
    drugNameTextView.text = getString(R.string.prescribed_drug_with_dosages_sheet_drug_name, drugName)
  }

  fun populateDosageList(list: List<DosageListItem>) {
    adapter.submitList(list)
  }

  companion object {
    private const val KEY_DRUG_NAME = "drugName"
    private const val KEY_PATIENT_UUID = "patientUuid"
    private const val KEY_PRESCRIBED_DRUG_UUID = "prescribedDrugUuid"

    fun intent(
        context: Context,
        drugName: String,
        patientUuid: UUID,
        prescribedDrugUuid: UUID?
    ): Intent {
      val intent = Intent(context, DosagePickerSheet::class.java)
      intent.putExtra(KEY_DRUG_NAME, drugName)
      intent.putExtra(KEY_PATIENT_UUID, patientUuid)
      intent.putExtra(KEY_PRESCRIBED_DRUG_UUID, prescribedDrugUuid)
      return intent
    }
  }
}
