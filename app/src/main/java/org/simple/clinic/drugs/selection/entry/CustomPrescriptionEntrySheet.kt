package org.simple.clinic.drugs.selection.entry

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.drugs.selection.entry.confirmremovedialog.ConfirmRemovePrescriptionDialog
import org.simple.clinic.drugs.selection.entry.di.CustomPrescriptionEntrySheetComponent
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.LinearLayoutWithPreImeKeyEventListener
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.textChanges
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class CustomPrescriptionEntrySheet : BottomSheetActivity() {

  private val rootLayout by bindView<LinearLayoutWithPreImeKeyEventListener>(R.id.customprescription_root)
  private val drugNameEditText by bindView<TextInputEditText>(R.id.customprescription_drug_name)
  private val drugDosageEditText by bindView<TextInputEditText>(R.id.customprescription_drug_dosage)
  private val saveButton by bindView<MaterialButton>(R.id.customprescription_save)
  private val enterMedicineTextView by bindView<TextView>(R.id.customprescription_enter_prescription)
  private val editMedicineTextView by bindView<TextView>(R.id.customprescription_edit_prescription)
  private val removeMedicineButton by bindView<MaterialButton>(R.id.customprescription_remove_button)

  @Inject
  lateinit var controllerFactory: CustomPrescriptionEntryController.Factory

  @Inject
  lateinit var locale: Locale

  private lateinit var component: CustomPrescriptionEntrySheetComponent

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private val openAs by unsafeLazy {
    intent.getParcelableExtra(KEY_OPEN_AS) as OpenAs
  }

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_custom_prescription_entry)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            sheetCreates(),
            drugNameChanges(),
            drugDosageChanges(),
            drugDosageFocusChanges(),
            saveClicks(),
            removeClicks()
        ),
        controller = controllerFactory.create(openAs),
        screenDestroys = onDestroys
    )

    // Dismiss this sheet when the keyboard is dismissed.
    rootLayout.backKeyPressInterceptor = { super.onBackgroundClick() }
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
        .customPrescriptionEntrySheetComponentBuilder()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  override fun onBackgroundClick() {
    val drugNameEmpty = drugNameEditText.text.isNullOrBlank()
    val dosageEmpty = drugDosageEditText.text.isNullOrBlank()
        || drugDosageEditText.text.toString().trim() == getString(R.string.customprescription_dosage_placeholder)

    if (drugNameEmpty && dosageEmpty) {
      super.onBackgroundClick()
    }
  }

  private fun sheetCreates() = Observable.just(ScreenCreated())

  private fun drugNameChanges() = drugNameEditText.textChanges(::CustomPrescriptionDrugNameTextChanged)

  private fun drugDosageChanges() = drugDosageEditText.textChanges(::CustomPrescriptionDrugDosageTextChanged)

  private fun drugDosageFocusChanges() = RxView.focusChanges(drugDosageEditText)
      .map(::CustomPrescriptionDrugDosageFocusChanged)

  private fun saveClicks(): Observable<UiEvent> {
    val dosageImeClicks = RxTextView.editorActions(drugDosageEditText) { it == EditorInfo.IME_ACTION_DONE }

    return RxView.clicks(saveButton)
        .mergeWith(dosageImeClicks)
        .map { SaveCustomPrescriptionClicked }
  }

  private fun removeClicks(): Observable<UiEvent> =
      RxView
          .clicks(removeMedicineButton)
          .map { RemoveCustomPrescriptionClicked }

  fun setSaveButtonEnabled(enabled: Boolean) {
    saveButton.isEnabled = enabled
  }

  fun setDrugDosageText(text: String) {
    drugDosageEditText.setText(text)
  }

  fun moveDrugDosageCursorToBeginning() {
    // Posting to EditText's handler is intentional. The cursor gets overridden otherwise.
    drugDosageEditText.post { drugDosageEditText.setSelection(0) }
  }

  fun showEnterNewPrescriptionTitle() {
    enterMedicineTextView.visibility = VISIBLE
  }

  fun showEditPrescriptionTitle() {
    editMedicineTextView.visibility = VISIBLE
  }

  fun showRemoveButton() {
    removeMedicineButton.visibility = VISIBLE
  }

  fun hideRemoveButton() {
    removeMedicineButton.visibility = GONE
  }

  fun setMedicineName(drugName: String) {
    drugNameEditText.setTextAndCursor(drugName)
  }

  fun setDosage(dosage: String?) {
    drugDosageEditText.setTextAndCursor(dosage ?: "")
  }

  fun showConfirmRemoveMedicineDialog(prescribedDrugUuid: UUID) {
    ConfirmRemovePrescriptionDialog.showForPrescription(prescribedDrugUuid, supportFragmentManager)
  }

  companion object {
    private const val KEY_OPEN_AS = "openAs"

    fun intentForAddNewPrescription(context: Context, patientUuid: UUID): Intent {
      val intent = Intent(context, CustomPrescriptionEntrySheet::class.java)
      intent.putExtra(KEY_OPEN_AS, OpenAs.New(patientUuid))
      return intent
    }

    fun intentForUpdatingPrescription(context: Context, prescribedDrugUuid: UUID): Intent {
      val intent = Intent(context, CustomPrescriptionEntrySheet::class.java)
      intent.putExtra(KEY_OPEN_AS, OpenAs.Update(prescribedDrugUuid))
      return intent
    }
  }
}
