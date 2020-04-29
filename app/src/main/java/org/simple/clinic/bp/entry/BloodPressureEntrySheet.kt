package org.simple.clinic.bp.entry

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.sheet_blood_pressure_entry.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemoveBloodPressureDialog
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemoveBloodPressureDialog.RemoveBloodPressureListener
import org.simple.clinic.bp.entry.di.BloodPressureEntryComponent
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.setTextAndCursor
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodPressureEntrySheet : BottomSheetActivity(), BloodPressureEntryUi, RemoveBloodPressureListener {

  @field:[Inject Named("exact_date")]
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var locale: Locale

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
  lateinit var bpValidator: BpValidator

  @Inject
  lateinit var userTimeZone: ZoneId

  @Inject
  lateinit var dateValidator: UserInputDateValidator

  @Inject
  lateinit var bloodPressureRepository: BloodPressureRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var appointmentsRepository: AppointmentRepository

  @Inject
  lateinit var patientRepository: PatientRepository

  private lateinit var component: BloodPressureEntryComponent

  private val uiRenderer = BloodPressureEntryUiRenderer(this)

  private val delegate by unsafeLazy {
    val openAs = intent.extras!!.getParcelable<OpenAs>(KEY_OPEN_AS)!!
    val defaultModel = BloodPressureEntryModel.create(openAs, LocalDate.now(userClock).year)

    val effectHandler = BloodPressureEntryEffectHandler.create(
        this,
        userSession,
        facilityRepository,
        patientRepository,
        bloodPressureRepository,
        appointmentsRepository,
        userClock,
        userInputDatePaddingCharacter,
        schedulersProvider
    )

    MobiusDelegate.forActivity(
        events.ofType(),
        defaultModel,
        BloodPressureEntryUpdate(bpValidator, dateValidator, LocalDate.now(userTimeZone), userInputDatePaddingCharacter),
        effectHandler,
        BloodPressureEntryInit(),
        uiRenderer::render
    )
  }

  private val events: Observable<UiEvent> by unsafeLazy {
    Observable.mergeArray(
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

    delegate.onRestoreInstanceState(savedInstanceState)
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
        .bloodPressureEntryComponent()
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
    screenDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  override fun onBackgroundClick() {
    if (systolicEditText.text.isBlank() && diastolicEditText.text.isBlank()) {
      super.onBackgroundClick()
    }
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
          .map { RemoveBloodPressureClicked }

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
    markBpAsSavedAndFinish()
  }

  override fun hideBpErrorMessage() {
    bpErrorTextView.visibility = View.GONE
  }

  override fun showSystolicLessThanDiastolicError() {
    showBpErrorMessage(getString(R.string.bloodpressureentry_error_systolic_more))
  }

  override fun showSystolicLowError() {
    showBpErrorMessage(getString(R.string.bloodpressureentry_error_systolic_70))
  }

  override fun showSystolicHighError() {
    showBpErrorMessage(getString(R.string.bloodpressureentry_error_systolic_300))
  }

  override fun showDiastolicLowError() {
    showBpErrorMessage(getString(R.string.bloodpressureentry_error_diastolic_40))
  }

  override fun showDiastolicHighError() {
    showBpErrorMessage(getString(R.string.bloodpressureentry_error_diastolic_180))
  }

  override fun showSystolicEmptyError() {
    showBpErrorMessage(getString(R.string.bloodpressureentry_error_systolic_empty))
  }

  override fun showDiastolicEmptyError() {
    showBpErrorMessage(getString(R.string.bloodpressureentry_error_diastolic_empty))
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
        .loadAnimation(this, R.anim.measurementinput_reading_entry_from_left)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_date_exit_to_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.displayedChildResId = R.id.bloodpressureentry_flipper_bp_entry
  }

  override fun showDateEntryScreen() {
    viewFlipper.inAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_date_entry_from_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_reading_exit_to_left)
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

  override fun setDateOnInputFields(dayOfMonth: String, month: String, fourDigitYear: String) {
    dayEditText.setTextAndCursor(getPaddedString(dayOfMonth))
    monthEditText.setTextAndCursor(getPaddedString(month))
    yearEditText.setTextAndCursor(fourDigitYear)
  }

  override fun showDateOnDateButton(date: LocalDate) {
    bpDateButton.text = dateFormatter.format(date)
  }

  override fun dismiss() {
    finish()
  }

  override fun onBloodPressureRemoved() {
    markBpAsSavedAndFinish()
  }

  private fun markBpAsSavedAndFinish() {
    val intent = Intent()
    intent.putExtra(EXTRA_WAS_BP_SAVED, true)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  private fun showBpErrorMessage(message: String) {
    with(bpErrorTextView) {
      text = message
      visibility = View.VISIBLE
    }
  }

  private fun getPaddedString(value: String): String =
      value.padStart(length = 2, padChar = userInputDatePaddingCharacter.value)
}
