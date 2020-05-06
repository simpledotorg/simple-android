package org.simple.clinic.drugs.selection.dosage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.sheet_dosage_picker.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.drugs.selection.dosage.di.DosagePickerSheetComponent
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.dp
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class DosagePickerSheet : BottomSheetActivity(), DosagePickerUi {

  @Inject
  lateinit var controllerFactory: DosagePickerSheetController.Factory

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var effectHandlerFactory: DosagePickerEffectHandler.Factory

  private lateinit var component: DosagePickerSheetComponent

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private val dosageAdapter = ItemAdapter(DosageDiffer())

  private val events by unsafeLazy {
    Observable
        .merge(
            sheetCreates(),
            dosageAdapter.itemEvents
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val uiRenderer = DosagePickerUiRenderer(this)

  private val delegate by unsafeLazy {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME) as String
    val prescribedDrugUuid = intent.getSerializableExtra(KEY_PRESCRIBED_DRUG_UUID) as UUID?

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = DosagePickerModel.create(drugName = drugName, existingPrescriptionUuid = prescribedDrugUuid),
        update = DosagePickerUpdate(),
        init = DosagePickerInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
    setContentView(R.layout.sheet_dosage_picker)

    recyclerView.apply {
      adapter = dosageAdapter
      layoutManager = LinearLayoutManager(this@DosagePickerSheet)
      addItemDecoration(DividerItemDecorator(this@DosagePickerSheet, 24.dp, 24.dp))
    }
    displayDrugName()

    val drugName = intent.getStringExtra(KEY_DRUG_NAME) as String
    val patientUuid = intent.getSerializableExtra(KEY_PATIENT_UUID) as UUID
    val prescribedDrugUuid = intent.getSerializableExtra(KEY_PRESCRIBED_DRUG_UUID).toOptional() as Optional<UUID>

    bindUiToController(
        ui = this,
        events = events,
        controller = controllerFactory.create(drugName, patientUuid, prescribedDrugUuid),
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

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun sheetCreates(): Observable<UiEvent> {
    return Observable.just(ScreenCreated())
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
