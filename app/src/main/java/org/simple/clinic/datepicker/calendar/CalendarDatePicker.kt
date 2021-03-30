package org.simple.clinic.datepicker.calendar

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.overrideCancellation
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.util.unsafeLazy
import java.time.LocalDate
import javax.inject.Inject

class CalendarDatePicker : DialogFragment() {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userClock: UserClock

  private val screenKey: Key by unsafeLazy { ScreenKey.key(this) }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = datePickerDialog()

    dialog.overrideCancellation(::backPressed)
    dialog.setButton(BUTTON_NEGATIVE, getString(android.R.string.cancel)) { _, _ -> backPressed() }

    return dialog
  }

  private fun datePickerDialog(): DatePickerDialog {
    val preselectedDate = screenKey.preselectedDate
    val minDate = screenKey.minDate
    val maxDate = screenKey.maxDate

    return DatePickerDialog(
        requireContext(),
        { _, year, month, dayOfMonth ->
          val setDate = LocalDate.of(year, month + 1, dayOfMonth)
          router.popWithResult(Succeeded(SelectedDate(setDate)))
        },
        preselectedDate.year,
        preselectedDate.monthValue - 1,
        preselectedDate.dayOfMonth
    ).apply {
      datePicker.minDate = minDate.toUtcInstant(userClock).toEpochMilli()
      datePicker.maxDate = maxDate.toUtcInstant(userClock).toEpochMilli()
    }
  }

  private fun backPressed() {
    requireActivity().onBackPressed()
  }

  @Parcelize
  data class Key(
      val preselectedDate: LocalDate,
      val minDate: LocalDate,
      val maxDate: LocalDate
  ) : ScreenKey() {

    override val analyticsName = "Calendar Date Picker"

    override val type = ScreenType.Modal

    override fun instantiateFragment() = CalendarDatePicker()
  }

  @Parcelize
  data class SelectedDate(val date: LocalDate) : Parcelable

  interface Injector {
    fun inject(target: CalendarDatePicker)
  }
}
