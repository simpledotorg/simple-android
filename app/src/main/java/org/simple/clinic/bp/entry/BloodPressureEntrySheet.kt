package org.simple.clinic.bp.entry

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.toObservable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemoveBloodPressureDialog
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemoveBloodPressureDialog.RemoveBloodPressureListener
import org.simple.clinic.bp.entry.di.BloodPressureEntryComponent
import org.simple.clinic.databinding.SheetBloodPressureEntryBinding
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.visibleOrGone
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodPressureEntrySheet :
    BaseBottomSheet<
        BloodPressureEntrySheet.Key,
        SheetBloodPressureEntryBinding,
        BloodPressureEntryModel,
        BloodPressureEntryEvent,
        BloodPressureEntryEffect,
        BloodPressureEntryUiRenderer>(),
    BloodPressureEntryUi,
    RemoveBloodPressureListener {

  @Inject
  @Named("exact_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  enum class ScreenType {
    BP_ENTRY,
    DATE_ENTRY
  }

  companion object {
    private const val KEY_OPEN_AS = "openAs"

    fun intentForNewBp(context: Context, patientUuid: UUID): Intent {
      return Intent(context, BloodPressureEntrySheet::class.java)
          .putExtra(KEY_OPEN_AS, New(patientUuid))
    }

    fun intentForUpdateBp(context: Context, bloodPressureMeasurementUuid: UUID): Intent {
      return Intent(context, BloodPressureEntrySheet::class.java)
          .putExtra(KEY_OPEN_AS, Update(bloodPressureMeasurementUuid))
    }

    fun wasBloodPressureSaved(result: Succeeded): Boolean {
      val savedBpResult = result.result as BloodPressureSavedResult
      return savedBpResult.saved
    }
  }

  @Inject
  lateinit var userInputDatePaddingCharacter: UserInputDatePaddingCharacter

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var userTimeZone: ZoneId

  @Inject
  lateinit var dateValidator: UserInputDateValidator

  @Inject
  lateinit var effectHandlerFactory: BloodPressureEntryEffectHandler.Factory

  override fun defaultModel() = BloodPressureEntryModel.create(screenKey.openAs, LocalDate.now(userClock).year)

  override fun uiRenderer() = BloodPressureEntryUiRenderer(this)

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
      SheetBloodPressureEntryBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(
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
      .cast<BloodPressureEntryEvent>()

  override fun createUpdate() = BloodPressureEntryUpdate(dateValidator, LocalDate.now(userTimeZone), userInputDatePaddingCharacter)

  override fun createInit() = BloodPressureEntryInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  private lateinit var component: BloodPressureEntryComponent

  private val systolicEditText
    get() = binding.systolicEditText

  private val diastolicEditText
    get() = binding.diastolicEditText

  private val dayEditText
    get() = binding.dayEditText

  private val monthEditText
    get() = binding.monthEditText

  private val yearEditText
    get() = binding.yearEditText

  private val removeBloodPressureButton
    get() = binding.removeBloodPressureButton

  private val bpDateButton
    get() = binding.bpDateButton

  private val backImageButton
    get() = binding.backImageButton

  private val rootLayout
    get() = binding.rootLayout

  private val viewFlipper
    get() = binding.viewFlipper

  private val bpErrorTextView
    get() = binding.bpErrorTextView

  private val enterBloodPressureTitleTextView
    get() = binding.enterBloodPressureTitleTextView

  private val editBloodPressureTitleTextView
    get() = binding.editBloodPressureTitleTextView

  private val dateErrorTextView
    get() = binding.dateErrorTextView

  private val progressLoader
    get() = binding.progressLoader

  private val bloodPressureEntryLayout
    get() = binding.bloodPressureEntryLayout

  override fun onBackgroundClick() {
    if (systolicEditText.text.isNullOrBlank() && diastolicEditText.text.isNullOrBlank()) {
      super.onBackgroundClick()
    }
  }

  private fun systolicTextChanges() = systolicEditText
      .textChanges()
      .map(CharSequence::toString)
      .map(::SystolicChanged)

  private fun diastolicTextChanges() = diastolicEditText
      .textChanges()
      .map(CharSequence::toString)
      .map(::DiastolicChanged)

  private fun imeDoneClicks(): Observable<SaveClicked> {
    return listOf(systolicEditText, diastolicEditText, dayEditText, monthEditText, yearEditText)
        .map { it.editorActions { actionId -> actionId == EditorInfo.IME_ACTION_DONE } }
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
      removeBloodPressureButton
          .clicks()
          .map { RemoveBloodPressureClicked }

  private fun bpDateClicks(): Observable<UiEvent> =
      bpDateButton
          .clicks()
          .map { BloodPressureDateClicked }

  private fun backClicks(): Observable<UiEvent> =
      backImageButton
          .clicks()
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
      dayEditText
          .textChanges()
          .map(CharSequence::toString)
          .map(::DayChanged)

  private fun monthTextChanges() =
      monthEditText
          .textChanges()
          .map(CharSequence::toString)
          .map(::MonthChanged)

  private fun yearTextChanges() =
      yearEditText
          .textChanges()
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
    val result = BloodPressureSavedResult.notSaved()
    router.popWithResult(Succeeded(result))
  }

  override fun showProgress() {
    progressLoader.visibleOrGone(isVisible = true)
    bloodPressureEntryLayout.visibleOrGone(isVisible = false)
    bpDateButton.visibleOrGone(isVisible = false)
    removeBloodPressureButton.visibleOrGone(isVisible = false)
  }

  override fun hideProgress() {
    progressLoader.visibleOrGone(isVisible = false)
    bloodPressureEntryLayout.visibleOrGone(isVisible = true)
    bpDateButton.visibleOrGone(isVisible = true)
    if (removeBloodPressureButton.isEnabled)
      removeBloodPressureButton.visibleOrGone(isVisible = true)
  }

  override fun onBloodPressureRemoved() {
    markBpAsSavedAndFinish()
  }

  private fun markBpAsSavedAndFinish() {
    val result = BloodPressureSavedResult.saved()
    router.popWithResult(Succeeded(result))
  }

  private fun showBpErrorMessage(message: String) {
    with(bpErrorTextView) {
      text = message
      visibility = View.VISIBLE
    }
  }

  private fun getPaddedString(value: String): String =
      value.padStart(length = 2, padChar = userInputDatePaddingCharacter.value)

  @Parcelize
  data class Key(
      val openAs: OpenAs
  ) : ScreenKey() {

    override val analyticsName = "Blood Pressure Entry"

    override fun instantiateFragment(): Fragment {

    }

    override val type = ScreenType.Modal
  }

  @Parcelize
  data class BloodPressureSavedResult(val saved: Boolean) : Parcelable {

    companion object {
      fun saved() = BloodPressureSavedResult(true)

      fun notSaved() = BloodPressureSavedResult(false)
    }
  }
}
