package org.simple.clinic.textInputdatepicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.toObservable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetTextInputDatePickerBinding
import org.simple.clinic.datepicker.SelectedDate
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.setTextAndCursor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TextInputDatePickerSheet : BaseBottomSheet<
    TextInputDatePickerSheet.Key,
    SheetTextInputDatePickerBinding,
    TextInputDatePickerModel,
    TextInputDatePickerEvent,
    TextInputDatePickerEffect
    >(),
    TextInputDatePickerUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: TextInputDatePickerEffectHandler.Factory

  @Inject
  lateinit var dateValidator: TextInputDatePickerValidator

  @Inject
  lateinit var userInputDatePaddingCharacter: UserInputDatePaddingCharacter

  @Inject
  @DateFormatter(DateFormatter.Type.Day)
  lateinit var dayDateFormatter: DateTimeFormatter

  @Inject
  @DateFormatter(DateFormatter.Type.Month)
  lateinit var monthDateFormatter: DateTimeFormatter

  @Inject
  @DateFormatter(DateFormatter.Type.FullYear)
  lateinit var yearDateFormatter: DateTimeFormatter

  private val imageTextInputSheetClose
    get() = binding.imageTextInputSheetClose

  private val dateErrorTextView
    get() = binding.dateErrorTextView

  private val dayEditText
    get() = binding.dayEditText

  private val monthEditText
    get() = binding.monthEditText

  private val yearEditText
    get() = binding.yearEditText

  override fun defaultModel() = TextInputDatePickerModel.create(screenKey.minDate, screenKey.maxDate, screenKey.prefilledDate)

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) = SheetTextInputDatePickerBinding.inflate(inflater, container, false)

  override fun events() = Observable
      .mergeArray(
          sheetCloseClicks(),
          dayTextChanges(),
          monthTextChanges(),
          yearTextChanges(),
          imeDoneClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<TextInputDatePickerEvent>()

  override fun createUpdate() = TextInputDatePickerUpdate(
      dateValidator = dateValidator,
      inputDatePaddingCharacter = userInputDatePaddingCharacter
  )

  override fun createInit() = TextInputDatePickerInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  private fun sheetCloseClicks() = imageTextInputSheetClose
      .clicks()
      .map { DismissSheetClicked }

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

  private fun imeDoneClicks() = listOf(dayEditText, monthEditText, yearEditText)
      .map { it.editorActions { actionID -> actionID == EditorInfo.IME_ACTION_DONE } }
      .toObservable()
      .flatMap { it }
      .map { DoneClicked }

  override fun dismissSheet() {
    router.pop()
  }

  override fun hideErrorMessage() {
    dateErrorTextView.visibility = View.GONE
  }

  override fun showInvalidDateError() {
    dateErrorTextView.visibility = View.VISIBLE
    dateErrorTextView.text = getString(R.string.sheet_text_input_date_picker_enter_valid_date)
  }

  override fun showDateIsInPastError() {
    dateErrorTextView.visibility = View.VISIBLE
    dateErrorTextView.text = getString(R.string.sheet_text_input_date_picker_date_cannot_be_in_past)
  }

  override fun showMaximumDateRangeError() {
    dateErrorTextView.visibility = View.VISIBLE
    dateErrorTextView.text = getString(R.string.sheet_text_input_date_picker_date_cannot_be_after_one_year)
  }

  override fun userEnteredDateSelected(userEnteredDate: LocalDate) {
    router.popWithResult(Succeeded(SelectedDate(userEnteredDate)))
  }

  override fun setDateOnInputFields(date: LocalDate) {
    dayEditText.setTextAndCursor(dayDateFormatter.format(date))
    monthEditText.setTextAndCursor(monthDateFormatter.format(date))
    yearEditText.setTextAndCursor(yearDateFormatter.format(date))
  }

  @Parcelize
  data class Key(val minDate: LocalDate, val maxDate: LocalDate, val prefilledDate: LocalDate?) : ScreenKey() {

    override val analyticsName = "Text Input Date Picker"

    override fun instantiateFragment() = TextInputDatePickerSheet()

    override val type = ScreenType.Modal
  }

  interface Injector {
    fun inject(target: TextInputDatePickerSheet)
  }
}
