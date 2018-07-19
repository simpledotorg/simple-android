package org.simple.clinic.drugs.selection.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class CustomPrescriptionEntrySheet : BottomSheetActivity() {

  private val drugNameEditText by bindView<EditText>(R.id.customprescription_drug_name)
  private val drugDosageEditText by bindView<EditText>(R.id.customprescription_drug_dosage)
  private val saveButton by bindView<Button>(R.id.customprescription_save)

  @Inject
  lateinit var controller: CustomPrescriptionEntryController

  private val onDestroys = PublishSubject.create<Any>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_custom_prescription_entry)
    TheActivity.component.inject(this)

    Observable.mergeArray(sheetCreates(), drugNameChanges(), drugDosageChanges(), drugDosageFocusChanges(), saveClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(onDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun onDestroy() {
    onDestroys.onNext(Any())
    super.onDestroy()
  }

  override fun onBackgroundClick() {
    val drugNameEmpty = drugNameEditText.text.isEmpty()
    val dosageEmpty = drugDosageEditText.text.isEmpty()
        || drugDosageEditText.text.toString().trim() == getString(R.string.customprescription_dosage_placeholder)

    if (drugNameEmpty && dosageEmpty) {
      super.onBackgroundClick()
    }
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val patientUuid = intent.getSerializableExtra(KEY_PATIENT_UUID) as UUID
    return Observable.just(CustomPrescriptionSheetCreated(patientUuid))
  }

  private fun drugNameChanges() = RxTextView.textChanges(drugNameEditText)
      .map(CharSequence::toString)
      .map(::CustomPrescriptionDrugNameTextChanged)

  private fun drugDosageChanges() = RxTextView.textChanges(drugDosageEditText)
      .map(CharSequence::toString)
      .map(::CustomPrescriptionDrugDosageTextChanged)

  private fun drugDosageFocusChanges() = RxView.focusChanges(drugDosageEditText)
      .map(::CustomPrescriptionDrugDosageFocusChanged)

  private fun saveClicks(): Observable<UiEvent> {
    val dosageImeClicks = RxTextView.editorActions(drugDosageEditText) { it == EditorInfo.IME_ACTION_DONE }

    return RxView.clicks(saveButton)
        .mergeWith(dosageImeClicks)
        .map { SaveCustomPrescriptionClicked() }
  }

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

  companion object {
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun intent(context: Context, patientUuid: UUID): Intent {
      val intent = Intent(context, CustomPrescriptionEntrySheet::class.java)
      intent.putExtra(KEY_PATIENT_UUID, patientUuid)
      return intent
    }
  }
}
