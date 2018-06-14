package org.resolvetosavelives.red.bp.entry

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

/**
 * TODO: get rid of fragments completely.
 */
class BloodPressureEntrySheetView : BottomSheetDialogFragment() {

  @Inject
  lateinit var controller: BloodPressureEntrySheetController

  private val systolicEditText by bindView<EditText>(R.id.bloodpressureentry_systolic)
  private val diastolicEditText by bindView<EditText>(R.id.bloodpressureentry_diastolic)

  private val onStops = PublishSubject.create<Any>()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val sheetView = inflater.inflate(R.layout.sheet_blood_pressure_entry, container)
    dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    TheActivity.component.inject(this)
    return sheetView
  }

  override fun onStart() {
    super.onStart()

    Observable
        .mergeArray(
            sheetCreates(),
            systolicTextChanges(),
            diastolicTextChanges(),
            diastolicImeOptionClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(onStops)
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun onStop() {
    super.onStop()
    onStops.onNext(Any())
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val patientUuid = arguments!!.getSerializable(KEY_PATIENT_UUID) as UUID
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

  companion object {
    private const val TAG = "BloodPressureEntrySheetFragment"
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun showForPatient(patientUuid: UUID, fragmentManager: FragmentManager) {
      val existingFragment = fragmentManager.findFragmentByTag(TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val fragment = BloodPressureEntrySheetView()
      val args = Bundle(1)
      args.putSerializable(KEY_PATIENT_UUID, patientUuid)
      fragment.arguments = args

      fragmentManager
          .beginTransaction()
          .add(fragment, TAG)
          .commitNowAllowingStateLoss()
    }
  }
}
