package org.simple.clinic.overdue.download.formatdialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.DialogSelectOverdueDownloadFormatBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.overdue.download.OverdueListFileFormat
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SelectOverdueDownloadFormatDialog : BaseDialog<
    SelectOverdueDownloadFormatDialog.Key,
    DialogSelectOverdueDownloadFormatBinding,
    SelectOverdueDownloadFormatModel,
    SelectOverdueDownloadFormatEvent,
    SelectOverdueDownloadFormatEffect,
    SelectOverdueDownloadFormatViewEffect>(), SelectOverdueDownloadFormatUi, UiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: SelectOverdueDownloadFormatEffectHandler.Factory

  private val openAs by unsafeLazy { screenKey.openAs }

  private val fileFormatToRadioButtonId = mapOf(
      OverdueListFileFormat.CSV to R.id.downloadAsCsvRadioButton,
      OverdueListFileFormat.PDF to R.id.downloadAsPdfRadioButton
  )

  private val radioButtonIdToFileFormat = mapOf(
      R.id.downloadAsCsvRadioButton to OverdueListFileFormat.CSV,
      R.id.downloadAsPdfRadioButton to OverdueListFileFormat.PDF
  )

  private val dialogTitleTextView
    get() = binding.dialogTitleTextView

  private val radioGroup
    get() = binding.overdueDownloadFormatRadioGroup

  private val progressIndicator
    get() = binding.progressIndicator

  private val downloadOrShareButton
    get() = binding.downloadOrShareButton

  private val cancelButton
    get() = binding.cancelButton

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun createUpdate() = SelectOverdueDownloadFormatUpdate()

  override fun defaultModel() = SelectOverdueDownloadFormatModel.create(openAs)

  override fun createInit() = SelectOverdueDownloadFormatInit()

  override fun events() = Observable
      .merge(
          downloadOrShareClicks(),
          cancelClicks(),
          overdueListFormatChanges()
      )
      .compose(ReportAnalyticsEvents())
      .cast<SelectOverdueDownloadFormatEvent>()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<SelectOverdueDownloadFormatViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer)
      .build()

  override fun uiRenderer() = SelectOverdueDownloadFormatUiRenderer(this)

  override fun viewEffectHandler() = SelectOverdueDownloadFormatViewEffectHandler(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = DialogSelectOverdueDownloadFormatBinding
      .inflate(layoutInflater, container, false)

  override fun setOverdueListFormat(overdueListFileFormat: OverdueListFileFormat) {
    radioGroup.check(fileFormatToRadioButtonId[overdueListFileFormat]!!)
  }

  override fun setDownloadTitle() {
    dialogTitleTextView.setText(R.string.select_overdue_download_format_dialog_title_download)
  }

  override fun setDownloadButtonLabel() {
    downloadOrShareButton.setText(R.string.select_overdue_download_format_dialog_action_positive_download)
  }

  override fun setShareTitle() {
    dialogTitleTextView.setText(R.string.select_overdue_download_format_dialog_title_share)
  }

  override fun setShareButtonLabel() {
    downloadOrShareButton.setText(R.string.select_overdue_download_format_dialog_action_positive_share)
  }

  override fun hideTitle() {
    dialogTitleTextView.visibility = View.GONE
  }

  override fun hideContent() {
    radioGroup.visibility = View.GONE
  }

  override fun showProgress() {
    progressIndicator.visibility = View.VISIBLE
    isCancelable = false
  }

  override fun hideDownloadOrShareButton() {
    downloadOrShareButton.visibility = View.GONE
  }

  override fun showTitle() {
    dialogTitleTextView.visibility = View.VISIBLE
  }

  override fun showContent() {
    radioGroup.visibility = View.VISIBLE
  }

  override fun hideProgress() {
    progressIndicator.visibility = View.GONE
    isCancelable = true
  }

  override fun showDownloadOrShareButton() {
    downloadOrShareButton.visibility = View.VISIBLE
  }

  override fun shareDownloadedFile(downloadedUri: Uri, mimeType: String) {
    val intent = Intent().apply {
      action = Intent.ACTION_SEND
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, downloadedUri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val shareIntent = Intent.createChooser(intent, getString(R.string.select_overdue_download_format_dialog_share_with))
    startActivity(shareIntent)

    // Dismiss dialog *after* the share intent is triggered
    router.pop()
  }

  override fun dismiss() {
    router.pop()
  }

  override fun openNotEnoughStorageErrorDialog() {
    router.replaceTop(NotEnoughStorageErrorDialog.Key())
  }

  override fun openDownloadFailedErrorDialog() {
    router.replaceTop(DownloadFailedErrorDialog.Key())
  }

  private fun downloadOrShareClicks() = downloadOrShareButton
      .clicks()
      .map { DownloadOrShareClicked }

  private fun cancelClicks() = cancelButton
      .clicks()
      .map { CancelClicked }

  private fun overdueListFormatChanges() = radioGroup
      .checkedChanges()
      .skipInitialValue()
      .map { DownloadFormatChanged(radioButtonIdToFileFormat[it]!!) }

  @Parcelize
  data class Key(
      val openAs: OpenAs,
      override val analyticsName: String = "",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment() = SelectOverdueDownloadFormatDialog()
  }

  interface Injector {
    fun inject(target: SelectOverdueDownloadFormatDialog)
  }
}
