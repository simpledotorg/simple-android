package org.simple.clinic.drugs.selection.dosage

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.PrescribedDrugWithDosageListItemBinding
import org.simple.clinic.databinding.SheetDosagePickerBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.drugs.selection.dosage.di.DosagePickerSheetComponent
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.dp
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class DosagePickerSheet : BottomSheetActivity(), DosagePickerUi, DosagePickerUiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: DosagePickerEffectHandler.Factory

  private lateinit var component: DosagePickerSheetComponent

  private val dosageAdapter = ItemAdapter(
      diffCallback = DosageDiffer(),
      bindings = mapOf(
          R.layout.prescribed_drug_with_dosage_list_item to { layoutInflater, parent ->
            PrescribedDrugWithDosageListItemBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val events by unsafeLazy {
    Observable
        .merge(
            sheetCreates(),
            dosageClicks(),
            noneClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val uiRenderer = DosagePickerUiRenderer(this)

  private val delegate by unsafeLazy {
    val patientUuid = intent.getSerializableExtra(KEY_PATIENT_UUID) as UUID
    val drugName = intent.getStringExtra(KEY_DRUG_NAME) as String
    val prescribedDrugUuid = intent.getSerializableExtra(KEY_PRESCRIBED_DRUG_UUID) as UUID?

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = DosagePickerModel.create(
            patientUuid = patientUuid,
            drugName = drugName,
            existingPrescriptionUuid = prescribedDrugUuid
        ),
        update = DosagePickerUpdate(),
        init = DosagePickerInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private lateinit var binding: SheetDosagePickerBinding

  private val recyclerView
    get() = binding.recyclerView

  private val drugNameTextView
    get() = binding.drugNameTextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)

    binding = SheetDosagePickerBinding.inflate(layoutInflater)
    setContentView(binding.root)

    recyclerView.apply {
      adapter = dosageAdapter
      layoutManager = LinearLayoutManager(this@DosagePickerSheet)
      addItemDecoration(DividerItemDecorator(this@DosagePickerSheet, 24.dp, 24.dp))
    }
    displayDrugName()
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .dosagePickerSheetComponent()
        .create(activity = this)

    component.inject(this)
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  private fun sheetCreates(): Observable<UiEvent> {
    return Observable.just(ScreenCreated())
  }

  private fun noneClicks(): Observable<DosagePickerEvent> {
    return dosageAdapter
        .itemEvents
        .ofType<DosageListItem.Event.NoneClicked>()
        .map { NoneSelected }
  }

  private fun dosageClicks(): Observable<DosagePickerEvent> {
    return dosageAdapter
        .itemEvents
        .ofType<DosageListItem.Event.DosageClicked>()
        .map { DosageSelected(it.protocolDrug) }
  }

  private fun displayDrugName() {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME)
    drugNameTextView.text = getString(R.string.prescribed_drug_with_dosages_sheet_drug_name, drugName)
  }

  override fun populateDosageList(list: List<DosageListItem>) {
    dosageAdapter.submitList(list)
  }

  override fun close() {
    finish()
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
