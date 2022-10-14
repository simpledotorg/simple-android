package org.simple.clinic.patient.download.formatdialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.DialogSelectLineListFormatBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.patient.download.PatientLineListFileFormat
import javax.inject.Inject

class SelectLineListFormatDialog : BaseDialog<
    SelectLineListFormatDialog.Key,
    DialogSelectLineListFormatBinding,
    SelectLineListFormatModel,
    SelectLineListFormatEvent,
    SelectLineListFormatEffect,
    SelectLineListFormatViewEffect>(), SelectLineListUi, UiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: SelectLineListFormatEffectHandler.Factory

  private val fileFormatToRadioButtonId = mapOf(
      PatientLineListFileFormat.CSV to R.id.downloadAsCsvRadioButton,
      PatientLineListFileFormat.PDF to R.id.downloadAsPdfRadioButton
  )

  private val radioButtonIdToFileFormat = mapOf(
      R.id.downloadAsCsvRadioButton to PatientLineListFileFormat.CSV,
      R.id.downloadAsPdfRadioButton to PatientLineListFileFormat.PDF
  )

  private val radioGroup
    get() = binding.patientListDownloadFormatRadioGroup

  private val downloadButton
    get() = binding.downloadButton

  private val cancelButton
    get() = binding.cancelButton

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun createUpdate() = SelectLineListFormatUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<SelectLineListFormatViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer)
      .build()

  override fun uiRenderer() = SelectLineListUiRenderer(this)

  override fun viewEffectHandler() = SelectLineListFormatViewEffectHandler(this)

  override fun defaultModel() = SelectLineListFormatModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = DialogSelectLineListFormatBinding
      .inflate(layoutInflater, container, false)

  override fun events() = Observable
      .merge(
          downloadClicks(),
          cancelClicks(),
          patientLineListFormatChanges()
      )
      .compose(ReportAnalyticsEvents())
      .cast<SelectLineListFormatEvent>()

  override fun setLineListFormat(fileFormat: PatientLineListFileFormat) {
    radioGroup.check(fileFormatToRadioButtonId[fileFormat]!!)
  }

  override fun dismiss() {
    router.pop()
  }

  private fun downloadClicks() = downloadButton
      .clicks()
      .map { DownloadButtonClicked }

  private fun cancelClicks() = cancelButton
      .clicks()
      .map { CancelButtonClicked }

  private fun patientLineListFormatChanges() = radioGroup
      .checkedChanges()
      .skipInitialValue()
      .map { DownloadFileFormatChanged(radioButtonIdToFileFormat[it]!!) }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Select Line List Format Dialog",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment() = SelectLineListFormatDialog()
  }

  interface Injector {
    fun inject(target: SelectLineListFormatDialog)
  }
}
