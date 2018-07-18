package org.simple.clinic.bp.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class BloodPressureEntrySheet : BottomSheetActivity() {

  @Inject
  lateinit var controller: BloodPressureEntrySheetController

  private val systolicEditText by bindView<EditText>(R.id.bloodpressureentry_systolic)
  private val diastolicEditText by bindView<EditText>(R.id.bloodpressureentry_diastolic)

  private val onDestroys = PublishSubject.create<Any>()

  companion object {
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun intent(context: Context, patientUuid: UUID): Intent {
      val intent = Intent(context, BloodPressureEntrySheet::class.java)
      intent.putExtra(KEY_PATIENT_UUID, patientUuid)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_blood_pressure_entry)
    TheActivity.component.inject(this)

    Observable.merge(
        sheetCreates(),
        systolicTextChanges(),
        diastolicTextChanges(),
        diastolicImeOptionClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(onDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun onDestroy() {
    onDestroys.onNext(Any())
    super.onDestroy()
  }

  override fun onBackgroundClick() {
    if (systolicEditText.text.isBlank() && diastolicEditText.text.isBlank()) {
      super.onBackgroundClick()
    }
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val patientUuid = intent.extras.getSerializable(KEY_PATIENT_UUID) as UUID
    return Observable.just(BloodPressureEntrySheetCreated(patientUuid))
  }

  private fun systolicTextChanges() = RxTextView.textChanges(systolicEditText)
      .map(CharSequence::toString)
      .map(::BloodPressureSystolicTextChanged)

  private fun diastolicTextChanges() = RxTextView.textChanges(diastolicEditText)
      .map(CharSequence::toString)
      .map(::BloodPressureDiastolicTextChanged)

  private fun diastolicImeOptionClicks(): Observable<BloodPressureSaveClicked> {
    return Observable
        .merge(
            RxTextView.editorActions(systolicEditText, { actionId -> actionId == EditorInfo.IME_ACTION_DONE }),
            RxTextView.editorActions(diastolicEditText, { actionId -> actionId == EditorInfo.IME_ACTION_DONE }))
        .map { BloodPressureSaveClicked() }
  }

  fun changeFocusToDiastolic() {
    diastolicEditText.requestFocus()
  }
}
