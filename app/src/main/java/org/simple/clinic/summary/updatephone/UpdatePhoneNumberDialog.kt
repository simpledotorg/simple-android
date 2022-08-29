package org.simple.clinic.summary.updatephone

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.DialogPatientsummaryUpdatephoneBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setTextAndCursor
import java.util.UUID
import javax.inject.Inject

class UpdatePhoneNumberDialog : BaseDialog<
    UpdatePhoneNumberDialog.Key,
    DialogPatientsummaryUpdatephoneBinding,
    UpdatePhoneNumberModel,
    UpdatePhoneNumberEvent,
    UpdatePhoneNumberEffect,
    Nothing>(), UpdatePhoneNumberDialogUi, UpdatePhoneNumberUiActions {

  private val numberEditText
    get() = binding.numberEditText

  private val phoneInputLayout
    get() = binding.phoneInputLayout

  @Inject
  lateinit var effectHandlerFactory: UpdatePhoneNumberEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val hotEvents = PublishSubject.create<UiEvent>()

  override fun defaultModel() = UpdatePhoneNumberModel.create(
      patientUuid = screenKey.patientUuid
  )

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = DialogPatientsummaryUpdatephoneBinding
      .inflate(layoutInflater, container, false)

  override fun uiRenderer() = UpdatePhoneNumberUiRenderer(this)

  override fun events(): Observable<UpdatePhoneNumberEvent> {
    return hotEvents
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun createInit() = UpdatePhoneNumberInit()

  override fun createUpdate() = UpdatePhoneNumberUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Nothing>) = effectHandlerFactory
      .create(this)
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.patientsummary_updatephone_dialog_title)
        .setMessage(R.string.patientsummary_updatephone_dialog_message)
        .setPositiveButton(R.string.patientsummary_updatephone_save, null)
        .setNegativeButton(R.string.patientsummary_updatephone_cancel, null)
        .create()
  }

  override fun onResume() {
    super.onResume()

    val alertDialog = (dialog as AlertDialog)
    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
      hotEvents.onNext(UpdatePhoneNumberSaveClicked(number = numberEditText.text?.toString().orEmpty()))
    }

    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
      hotEvents.onNext(UpdatePhoneNumberCancelClicked)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = super.onCreateView(inflater, container, savedInstanceState)
    (dialog as? AlertDialog)?.setView(view)
    return view
  }

  override fun showBlankPhoneNumberError() {
    phoneInputLayout.error = getString(R.string.patientsummary_updatephone_error_phonenumber_empty)
  }

  override fun showPhoneNumberTooShortError(minimumAllowedNumberLength: Int) {
    phoneInputLayout.error = getString(R.string.patientsummary_updatephone_error_phonenumber_length_less, minimumAllowedNumberLength.toString())
  }

  override fun preFillPhoneNumber(number: String) {
    numberEditText.setTextAndCursor(number)
  }

  override fun closeDialog() {
    router.pop()
  }

  interface Injector {
    fun inject(target: UpdatePhoneNumberDialog)
  }

  @Parcelize
  data class Key(
      val patientUuid: UUID,
      override val analyticsName: String = "Update Phone Number Dialog",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return UpdatePhoneNumberDialog().apply {
        isCancelable = false
      }
    }
  }
}
