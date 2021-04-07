package org.simple.clinic.textInputdatepicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetTextInputDatePickerBinding
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
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

  override fun defaultModel() = TextInputDatePickerModel.create()

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) = SheetTextInputDatePickerBinding.inflate(inflater, container, false)

  override fun events() = Observable
      .merge(
          sheetCloseClicks(),
          dayTextChanges(),
          monthTextChanges(),
          yearTextChanges()
      )
      .compose(ReportAnalyticsEvents())
      .cast<TextInputDatePickerEvent>()
  
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

  @Parcelize
  object Key : ScreenKey() {

    override val analyticsName = "Text Input Date Picker"

    override fun instantiateFragment() = TextInputDatePickerSheet()

    override val type = ScreenType.Modal
  }
}
