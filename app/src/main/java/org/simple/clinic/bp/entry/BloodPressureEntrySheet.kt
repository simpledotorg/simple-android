package org.simple.clinic.bp.entry

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.mobius.MobiusActivityDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ViewFlipperWithLayoutEditorPreview
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.setTextAndCursor
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BloodPressureEntrySheet : BottomSheetActivity(), BloodPressureEntryUi {
  @Inject
  lateinit var controller: BloodPressureEntrySheetController

  @Inject
  lateinit var dateFormatter: DateTimeFormatter

  private val rootLayout by bindView<LinearLayoutWithPreImeKeyEventListener>(R.id.bloodpressureentry_root)
  private val systolicEditText by bindView<EditText>(R.id.bloodpressureentry_systolic)
  private val diastolicEditText by bindView<EditTextWithBackspaceListener>(R.id.bloodpressureentry_diastolic)
  private val bpErrorTextView by bindView<TextView>(R.id.bloodpressureentry_bp_error)
  private val bpDateButton by bindView<Button>(R.id.bloodpressureentry_bp_date)
  private val backImageButton by bindView<ImageButton>(R.id.bloodpressureentry_back_button)
  private val enterBloodPressureTitleTextView by bindView<TextView>(R.id.bloodpressureentry_enter_blood_pressure)
  private val editBloodPressureTitleTextView by bindView<TextView>(R.id.bloodpressureentry_edit_blood_pressure)
  private val removeBloodPressureButton by bindView<Button>(R.id.bloodpressureentry_remove)
  private val dayEditText by bindView<EditText>(R.id.bloodpressureentry_day)
  private val monthEditText by bindView<EditText>(R.id.bloodpressureentry_month)
  private val yearEditText by bindView<EditText>(R.id.bloodpressureentry_year)
  private val viewFlipper by bindView<ViewFlipperWithLayoutEditorPreview>(R.id.bloodpressureentry_view_flipper)
  private val dateErrorTextView by bindView<TextView>(R.id.bloodpressureentry_date_error)

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()

  enum class ScreenType {
    BP_ENTRY,
    DATE_ENTRY
  }

  companion object {
    private const val KEY_OPEN_AS = "openAs"
    private const val EXTRA_WAS_BP_SAVED = "wasBpSaved"

    fun intentForNewBp(context: Context, patientUuid: UUID): Intent {
      return Intent(context, BloodPressureEntrySheet::class.java)
          .putExtra(KEY_OPEN_AS, New(patientUuid))
    }

    fun intentForUpdateBp(context: Context, bloodPressureMeasurementUuid: UUID): Intent {
      return Intent(context, BloodPressureEntrySheet::class.java)
          .putExtra(KEY_OPEN_AS, Update(bloodPressureMeasurementUuid))
    }

    fun wasBloodPressureSaved(data: Intent): Boolean {
      return data.getBooleanExtra(EXTRA_WAS_BP_SAVED, false)
    }
  }

  @Inject
  lateinit var userInputDatePaddingCharacter: UserInputDatePaddingCharacter

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var bpValidator: BpValidator

  @Inject
  lateinit var bloodPressureRepository: BloodPressureRepository

  private val viewRenderer = BloodPressureEntryViewRenderer(this)

  private val delegate by unsafeLazy {
    val defaultModel = when (val openAs = intent.extras!!.getParcelable<OpenAs>(KEY_OPEN_AS)!!) {
      is New -> BloodPressureEntryModel.newBloodPressureEntry(New(openAs.patientUuid))
      is Update -> BloodPressureEntryModel.updateBloodPressureEntry(Update(openAs.bpUuid))
    }

    val effectHandler = BloodPressureEntryEffectHandler
        .create(this, userClock, userInputDatePaddingCharacter, bloodPressureRepository, schedulersProvider)

    MobiusActivityDelegate(
        events.ofType(),
        defaultModel,
        BloodPressureEntryInit(),
        BloodPressureEntryUpdate(bpValidator),
        effectHandler,
        viewRenderer::render,
        crashReporter
    )
  }

  private val events: Observable<UiEvent> by unsafeLazy {
    Observable
        .mergeArray(
            sheetCreates(),
            systolicTextChanges(),
            diastolicTextChanges(),
            imeDoneClicks(),
            diastolicBackspaceClicks(),
            removeClicks(),
            bpDateClicks(),
            backClicks(),
            hardwareBackPresses(),
            screenTypeChanges(),
            dayTextChanges(),
            monthTextChanges(),
            yearTextChanges()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_blood_pressure_entry)
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = screenDestroys
    )

    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    screenDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  override fun onBackgroundClick() {
    if (systolicEditText.text.isBlank() && diastolicEditText.text.isBlank()) {
      super.onBackgroundClick()
    }
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val openAs = intent.extras!!.getParcelable<OpenAs>(KEY_OPEN_AS)!!
    return Observable
        .just(SheetCreated(openAs))
        // TODO: Update: Now that we've moved to ReplayUntilScreenIsDestroyed, is this still required?
        // This delay stops the race condition (?) that happens frequently with replay().refCount()
        // in the controller. Temporary workaround until we figure out what exactly is going on.
        .delay(100L, TimeUnit.MILLISECONDS)
        .cast()
  }

  private fun systolicTextChanges() = RxTextView.textChanges(systolicEditText)
      .map(CharSequence::toString)
      .map(::SystolicChanged)

  private fun diastolicTextChanges() = RxTextView.textChanges(diastolicEditText)
      .map(CharSequence::toString)
      .map(::DiastolicChanged)

  private fun imeDoneClicks(): Observable<SaveClicked> {
    return listOf(systolicEditText, diastolicEditText, dayEditText, monthEditText, yearEditText)
        .map { RxTextView.editorActions(it) { actionId -> actionId == EditorInfo.IME_ACTION_DONE } }
        .toObservable()
        .flatMap { it }
        .map { SaveClicked }
  }

  private fun diastolicBackspaceClicks(): Observable<UiEvent> {
    return diastolicEditText
        .backspaceClicks
        .map { DiastolicBackspaceClicked }
  }

  private fun removeClicks(): Observable<UiEvent> =
      RxView
          .clicks(removeBloodPressureButton)
          .map { RemoveClicked }

  private fun bpDateClicks(): Observable<UiEvent> =
      RxView
          .clicks(bpDateButton)
          .map { BloodPressureDateClicked }

  private fun backClicks(): Observable<UiEvent> =
      RxView
          .clicks(backImageButton)
          .map { ShowBpClicked }

  private fun hardwareBackPresses(): Observable<UiEvent> {
    return Observable.create { emitter ->
      val interceptor = {
        emitter.onNext(BackPressed)
      }
      emitter.setCancellable { rootLayout.backKeyPressInterceptor = null }
      rootLayout.backKeyPressInterceptor = interceptor
    }
  }

  private fun screenTypeChanges(): Observable<UiEvent> =
      viewFlipper
          .displayedChildChanges
          .map {
            ScreenChanged(when (viewFlipper.displayedChildResId) {
              R.id.bloodpressureentry_flipper_bp_entry -> BP_ENTRY
              R.id.bloodpressureentry_flipper_date_entry -> DATE_ENTRY
              else -> throw AssertionError()
            })
          }

  private fun dayTextChanges() =
      RxTextView.textChanges(dayEditText)
          .map(CharSequence::toString)
          .map(::DayChanged)

  private fun monthTextChanges() =
      RxTextView.textChanges(monthEditText)
          .map(CharSequence::toString)
          .map(::MonthChanged)

  private fun yearTextChanges() =
      RxTextView.textChanges(yearEditText)
          .map(CharSequence::toString)
          .map(::YearChanged)

  override fun changeFocusToDiastolic() {
    diastolicEditText.requestFocus()
  }

  override fun changeFocusToSystolic() {
    systolicEditText.requestFocus()
  }

  override fun setBpSavedResultAndFinish() {
    val intent = Intent()
    intent.putExtra(EXTRA_WAS_BP_SAVED, true)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun hideBpErrorMessage() {
    bpErrorTextView.visibility = View.GONE
  }

  override fun showSystolicLessThanDiastolicError() {
    bpErrorTextView.text = getString(R.string.bloodpressureentry_error_systolic_more)
    bpErrorTextView.visibility = View.VISIBLE
  }

  override fun showSystolicLowError() {
    bpErrorTextView.text = getString(R.string.bloodpressureentry_error_systolic_70)
    bpErrorTextView.visibility = View.VISIBLE
  }

  override fun showSystolicHighError() {
    bpErrorTextView.text = getString(R.string.bloodpressureentry_error_systolic_300)
    bpErrorTextView.visibility = View.VISIBLE
  }

  override fun showDiastolicLowError() {
    bpErrorTextView.text = getString(R.string.bloodpressureentry_error_diastolic_40)
    bpErrorTextView.visibility = View.VISIBLE
  }

  override fun showDiastolicHighError() {
    bpErrorTextView.text = getString(R.string.bloodpressureentry_error_diastolic_180)
    bpErrorTextView.visibility = View.VISIBLE
  }

  override fun showSystolicEmptyError() {
    bpErrorTextView.text = getString(R.string.bloodpressureentry_error_systolic_empty)
    bpErrorTextView.visibility = View.VISIBLE
  }

  override fun showDiastolicEmptyError() {
    bpErrorTextView.text = getString(R.string.bloodpressureentry_error_diastolic_empty)
    bpErrorTextView.visibility = View.VISIBLE
  }

  override fun setSystolic(systolic: String) {
    systolicEditText.setTextAndCursor(systolic)
  }

  override fun setDiastolic(diastolic: String) {
    diastolicEditText.setTextAndCursor(diastolic)
  }

  override fun showRemoveBpButton() {
    removeBloodPressureButton.visibility = View.VISIBLE
    removeBloodPressureButton.isEnabled = true
  }

  override fun hideRemoveBpButton() {
    removeBloodPressureButton.visibility = View.GONE
    removeBloodPressureButton.isEnabled = false
  }

  override fun showEnterNewBloodPressureTitle() {
    enterBloodPressureTitleTextView.visibility = View.VISIBLE
  }

  override fun showEditBloodPressureTitle() {
    editBloodPressureTitleTextView.visibility = View.VISIBLE
  }

  override fun showConfirmRemoveBloodPressureDialog(uuid: UUID) {
    ConfirmRemoveBloodPressureDialog.show(uuid, supportFragmentManager)
  }

  override fun showBpEntryScreen() {
    viewFlipper.inAnimation = AnimationUtils
        .loadAnimation(this, R.anim.bloodpressureentry_bp_entry_from_left)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.bloodpressureentry_date_exit_to_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.displayedChildResId = R.id.bloodpressureentry_flipper_bp_entry
  }

  override fun showDateEntryScreen() {
    viewFlipper.inAnimation = AnimationUtils
        .loadAnimation(this, R.anim.bloodpressureentry_date_entry_from_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.bloodpressureentry_bp_exit_to_left)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.displayedChildResId = R.id.bloodpressureentry_flipper_date_entry
    yearEditText.requestFocus()
  }

  override fun showInvalidDateError() {
    dateErrorTextView.setText(R.string.bloodpressureentry_error_date_invalid_pattern)
    dateErrorTextView.visibility = View.VISIBLE
  }

  override fun showDateIsInFutureError() {
    dateErrorTextView.setText(R.string.bloodpressureentry_error_date_is_in_future)
    dateErrorTextView.visibility = View.VISIBLE
  }

  override fun hideDateErrorMessage() {
    dateErrorTextView.visibility = View.GONE
  }

  override fun setDateOnInputFields(dayOfMonth: String, month: String, twoDigitYear: String) {
    dayEditText.setTextAndCursor(dayOfMonth)
    monthEditText.setTextAndCursor(month)
    yearEditText.setTextAndCursor(twoDigitYear)
  }

  override fun showDateOnDateButton(date: LocalDate) {
    bpDateButton.text = dateFormatter.format(date)
  }

  override fun dismiss() {
    finish()
  }
}
