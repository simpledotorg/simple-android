package org.simple.clinic.textInputdatepicker

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.cast
import kotlinx.android.parcel.Parcelize
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

  override fun defaultModel() = TextInputDatePickerModel.create()

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) = SheetTextInputDatePickerBinding.inflate(inflater, container, false)

  override fun events() = sheetCloseClicks()
      .compose(ReportAnalyticsEvents())
      .cast<TextInputDatePickerEvent>()

  private fun sheetCloseClicks() = imageTextInputSheetClose
      .clicks()
      .map { DismissSheetClicked }

  override fun dismissSheet() {
    router.pop()
  }

  @Parcelize
  object Key : ScreenKey() {

    override val analyticsName = "Text Input Date Picker"

    override fun instantiateFragment() = TextInputDatePickerSheet()

    override val type = ScreenType.Modal
  }
}
