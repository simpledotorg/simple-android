package org.simple.clinic.summary.addphone

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.DialogPatientsummaryAddphoneBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.showKeyboard
import java.util.UUID
import javax.inject.Inject

class AddPhoneNumberDialog : BaseDialog<
    AddPhoneNumberDialog.Key,
    DialogPatientsummaryAddphoneBinding,
    AddPhoneNumberModel,
    AddPhoneNumberEvent,
    AddPhoneNumberEffect,
    Nothing>(), AddPhoneNumberUi, UiActions {

  @Inject
  lateinit var effectHandlerFactory: AddPhoneNumberEffectHandler.Factory

  private val phoneNumberEditText
    get() = binding.phoneNumberEditText

  private val phoneNumberInputLayout
    get() = binding.phoneNumberInputLayout

  override fun defaultModel() = AddPhoneNumberModel.create(screenKey.patientUuid)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = DialogPatientsummaryAddphoneBinding
      .inflate(layoutInflater, container, false)

  override fun uiRenderer() = AddPhoneNumberUiRender(this)

  override fun events() = saveClicks()
      .compose(ReportAnalyticsEvents())
      .cast<AddPhoneNumberEvent>()

  override fun createUpdate() = AddPhoneNumberUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Nothing>) = effectHandlerFactory
      .create(this)
      .build()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requireContext().injector<Injector>().inject(this)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    val view = super.onCreateView(inflater, container, savedInstanceState)
    (dialog as? AlertDialog)?.setView(view)
    return view
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.patientsummary_addphone_dialog_title)
        .setMessage(R.string.patientsummary_addphone_dialog_message)
        .setPositiveButton(R.string.patientsummary_addphone_save, null)
        .setNegativeButton(R.string.patientsummary_addphone_cancel, null)
        .create()
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    phoneNumberEditText.showKeyboard()
  }

  private fun saveClicks(): Observable<UiEvent> {
    val saveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    return saveButton
        .clicks()
        .map { AddPhoneNumberSaveClicked(number = phoneNumberEditText.text.toString()) }
  }

  override fun showPhoneNumberBlank() {
    phoneNumberInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_empty)
  }

  override fun showPhoneNumberTooShortError(requiredNumberLength: Int) {
    phoneNumberInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_length_less, requiredNumberLength.toString())
  }

  override fun clearPhoneNumberError() {
    phoneNumberInputLayout.error = null
  }

  override fun closeDialog() {
    dismiss()
  }

  interface Injector {
    fun inject(target: AddPhoneNumberDialog)
  }

  @Parcelize
  data class Key(
      val patientUuid: UUID,
      override val analyticsName: String = "Add Phone Number Dialog",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return AddPhoneNumberDialog().apply {
        isCancelable = false
      }
    }
  }
}
