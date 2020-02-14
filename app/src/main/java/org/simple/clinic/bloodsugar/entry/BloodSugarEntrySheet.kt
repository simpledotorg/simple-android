package org.simple.clinic.bloodsugar.entry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.toObservable
import kotlinx.android.synthetic.main.sheet_blood_sugar_entry.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.BLOOD_SUGAR_ENTRY
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusActivityDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class BloodSugarEntrySheet : BottomSheetActivity(), BloodSugarEntryUi {
  enum class ScreenType {
    BLOOD_SUGAR_ENTRY,
    DATE_ENTRY
  }

  companion object {
    private const val KEY_OPEN_AS = "openAs"
    private const val EXTRA_WAS_BLOOD_SUGAR_SAVED = "wasBloodSugarSaved"

    fun intentForNewBloodSugar(
        context: Context,
        patientUuid: UUID,
        measurementType: BloodSugarMeasurementType
    ): Intent {
      val intent = Intent(context, BloodSugarEntrySheet::class.java)
      intent.putExtra(KEY_OPEN_AS, New(patientUuid, measurementType))
      return intent
    }
  }

  @Inject
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var userInputDatePaddingCharacter: UserInputDatePaddingCharacter

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var userTimeZone: ZoneId

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var bloodSugarEntryUpdate: BloodSugarEntryUpdate.Factory

  @Inject
  lateinit var bloodSugarEntryEffectHandler: BloodSugarEntryEffectHandler.Factory

  private val uiRenderer = BloodSugarEntryUiRenderer(this)

  private val delegate by unsafeLazy {
    val openAs = intent.getParcelableExtra<OpenAs>(KEY_OPEN_AS)!!
    val defaultModel = BloodSugarEntryModel.create(LocalDate.now(userClock).year, openAs)

    MobiusActivityDelegate(
        events = events.ofType(),
        defaultModel = defaultModel,
        init = BloodSugarEntryInit(),
        update = bloodSugarEntryUpdate.create(LocalDate.now(userTimeZone)),
        effectHandler = bloodSugarEntryEffectHandler.create(this).build(),
        modelUpdateListener = uiRenderer::render,
        crashReporter = crashReporter
    )
  }

  private val events: Observable<UiEvent> by unsafeLazy {
    Observable.mergeArray(
        bloodSugarTextChanges(),
        imeDoneClicks(),
        bloodSugarDateClicks(),
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_blood_sugar_entry)
    TheActivity.component.inject(this)

    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    super.onStop()
    delegate.stop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  private fun bloodSugarTextChanges() = bloodSugarReadingEditText.textChanges()
      .map(CharSequence::toString)
      .map(::BloodSugarChanged)

  private fun imeDoneClicks(): Observable<SaveClicked> {
    return listOf(bloodSugarReadingEditText, dayEditText, monthEditText, yearEditText)
        .map { it.editorActions { actionId -> actionId == EditorInfo.IME_ACTION_DONE } }
        .toObservable()
        .flatMap { it }
        .map { SaveClicked }
  }

  private fun bloodSugarDateClicks(): Observable<UiEvent> = bloodSugarDateButton
      .clicks()
      .map { BloodSugarDateClicked }

  private fun backClicks(): Observable<UiEvent> = backImageButton
      .clicks()
      .map { ShowBloodSugarEntryClicked }

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
              R.id.bloodsugarentry_flipper_blood_sugar_entry -> BLOOD_SUGAR_ENTRY
              R.id.bloodsugarentry_flipper_date_entry -> DATE_ENTRY
              else -> throw AssertionError()
            })
          }

  private fun dayTextChanges() = dayEditText
      .textChanges()
      .map(CharSequence::toString)
      .map(::DayChanged)

  private fun monthTextChanges() = monthEditText
      .textChanges()
      .map(CharSequence::toString)
      .map(::MonthChanged)

  private fun yearTextChanges() = yearEditText
      .textChanges()
      .map(CharSequence::toString)
      .map(::YearChanged)

  override fun setBloodSugarSavedResultAndFinish() {
    val intent = Intent()
    intent.putExtra(EXTRA_WAS_BLOOD_SUGAR_SAVED, true)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun hideBloodSugarErrorMessage() {
    bloodSugarErrorTextView.visibleOrGone(false)
  }

  override fun showBloodSugarEmptyError() {
    showBloodSugarErrorMessage(getString(R.string.bloodsugarentry_error_empty))
  }

  override fun showBloodSugarHighError() {
    showBloodSugarErrorMessage(getString(R.string.bloodsugarentry_error_higher_limit))
  }

  override fun showBloodSugarLowError() {
    showBloodSugarErrorMessage(getString(R.string.bloodsugarentry_error_lower_limit))
  }

  override fun showRandomBloodSugarTitle() {
    enterBloodSugarTitleTextView.visibility = View.VISIBLE
    editBloodSugarTitleTextView.visibility = View.GONE
    enterBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_random_title)
  }

  override fun showPostPrandialBloodSugarTitle() {
    enterBloodSugarTitleTextView.visibility = View.VISIBLE
    editBloodSugarTitleTextView.visibility = View.GONE
    enterBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_post_prandial_title)
  }

  override fun showFastingBloodSugarTitle() {
    enterBloodSugarTitleTextView.visibility = View.VISIBLE
    editBloodSugarTitleTextView.visibility = View.GONE
    enterBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_fasting_title)
  }

  override fun showBloodSugarEntryScreen() {
    viewFlipper.inAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_reading_entry_from_left)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_date_exit_to_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.displayedChildResId = R.id.bloodsugarentry_flipper_blood_sugar_entry
  }

  override fun showDateEntryScreen() {
    viewFlipper.inAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_date_entry_from_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_reading_exit_to_left)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.displayedChildResId = R.id.bloodsugarentry_flipper_date_entry
    yearEditText.requestFocus()
  }

  override fun showInvalidDateError() {
    showDateErrorMessage(getString(R.string.bloodsugarentry_error_date_invalid_pattern))
  }

  override fun showDateIsInFutureError() {
    showDateErrorMessage(getString(R.string.bloodsugarentry_error_date_is_in_future))
  }

  override fun hideDateErrorMessage() {
    dateErrorTextView.visibleOrGone(false)
  }

  override fun setDateOnInputFields(dayOfMonth: String, month: String, twoDigitYear: String) {
    dayEditText.setTextAndCursor(getPaddedString(dayOfMonth))
    monthEditText.setTextAndCursor(getPaddedString(month))
    yearEditText.setTextAndCursor(twoDigitYear)
  }

  override fun showDateOnDateButton(date: LocalDate) {
    bloodSugarDateButton.text = dateFormatter.format(date)
  }

  override fun showEditRadomBloodSugarTitle() {
    enterBloodSugarTitleTextView.visibility = View.GONE
    editBloodSugarTitleTextView.visibility = View.VISIBLE
    editBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_edit_random_title)
  }

  override fun showEditPostPrandialBloodSugarTitle() {
    enterBloodSugarTitleTextView.visibility = View.GONE
    editBloodSugarTitleTextView.visibility = View.VISIBLE
    editBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_edit_post_prandial_title)
  }

  override fun showEditFastingBloodSugarTitle() {
    enterBloodSugarTitleTextView.visibility = View.GONE
    editBloodSugarTitleTextView.visibility = View.VISIBLE
    editBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_edit_fasting_title)
  }

  override fun dismiss() {
    finish()
  }

  private fun showBloodSugarErrorMessage(message: String) {
    with(bloodSugarErrorTextView) {
      text = message
      visibility = View.VISIBLE
    }
  }

  private fun showDateErrorMessage(message: String) {
    with(dateErrorTextView) {
      text = message
      visibility = View.VISIBLE
    }
  }

  private fun getPaddedString(value: String): String =
      value.padStart(length = 2, padChar = userInputDatePaddingCharacter.value)
}
