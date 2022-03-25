package org.simple.clinic.appupdate.criticalupdatedialog

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ContactType
import org.simple.clinic.PLAY_STORE_URL_FOR_SIMPLE
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.databinding.DialogCriticalAppUpdateBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.resolveColor
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import javax.inject.Inject

class CriticalAppUpdateDialog : BaseDialog<
    CriticalAppUpdateDialog.Key,
    DialogCriticalAppUpdateBinding,
    CriticalAppUpdateModel,
    CriticalAppUpdateEvent,
    CriticalAppUpdateEffect,
    CriticalAppUpdateViewEffect>(),
    CriticalAppUpdateUi, UiActions {

  @Inject
  lateinit var effectHandlerFactory: CriticalAppUpdateEffectHandler.Factory

  @Inject
  lateinit var userClock: UserClock

  override fun defaultModel() = CriticalAppUpdateModel.create(screenKey.appUpdateNudgePriority)

  override fun uiRenderer() = CriticalAppUpdateUiRenderer(this, LocalDate.now(userClock))

  override fun viewEffectHandler() = CriticalAppUpdateViewEffectHandler(this)

  override fun createInit() = CriticalAppUpdateInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<CriticalAppUpdateViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer).build()

  override fun createUpdate() = CriticalAppUpdateDialogUpdate()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = DialogCriticalAppUpdateBinding
      .inflate(layoutInflater, container, false)

  override fun events() = Observable.mergeArray(
      updateButtonClicks(),
      contactSupportButtonClicks()
  ).compose(ReportAnalyticsEvents())
      .cast<CriticalAppUpdateEvent>()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    dialog!!.setCanceledOnTouchOutside(false)

    return super.onCreateView(inflater, container, savedInstanceState)
  }

  private val helpSectionGroup
    get() = binding.helpSectionGroup

  private val criticalUpdateReason
    get() = binding.criticalUpdateReason

  private val supportTeamContactButton
    get() = binding.supportTeamContactButton

  private val updateNowButton
    get() = binding.updateNowButton

  private fun updateButtonClicks(): Observable<UiEvent> {
    return updateNowButton
        .clicks()
        .map { UpdateAppClicked }
  }

  private fun contactSupportButtonClicks(): Observable<UiEvent> {
    return supportTeamContactButton
        .clicks()
        .map { ContactHelpClicked }
  }

  override fun showHelp() {
    helpSectionGroup.visibility = VISIBLE
  }

  override fun hideHelp() {
    helpSectionGroup.visibility = GONE
  }

  override fun renderCriticalAppUpdateReason(appStalenessInMonths: Int) {
    criticalUpdateReason.text = resources.getString(R.string.critical_update_required_reason, appStalenessInMonths)
  }

  override fun renderCriticalSecurityAppUpdateReason() {
    criticalUpdateReason.text = resources.getString(R.string.critical_security_update_required_reason)
  }

  override fun showSupportContactPhoneNumber(number: String, contactType: ContactType) {
    when (contactType) {
      ContactType.Telegram -> setupTelegramButton()
      ContactType.WhatsApp -> setupWhatsappButton()
      is ContactType.Unknown -> setupDefaultButton()
    }

    supportTeamContactButton.text = number
  }

  private fun setupWhatsappButton() {
    supportTeamContactButton.setBackgroundColor(requireContext().resolveColor(R.color.simple_green_100))
    supportTeamContactButton.setTextColor(requireContext().resolveColor(R.color.simple_green_500))
    supportTeamContactButton.setIconResource(R.drawable.ic_whatsapp_logo)
  }

  private fun setupTelegramButton() {
    supportTeamContactButton.setBackgroundColor(requireContext().resolveColor(R.color.simple_light_blue_100))
    supportTeamContactButton.setTextColor(requireContext().resolveColor(R.color.simple_light_blue_500))
    supportTeamContactButton.setIconResource(R.drawable.ic_telegram_logo)
  }

  private fun setupDefaultButton() {
    supportTeamContactButton.setBackgroundColor(requireContext().resolveColor(R.color.simple_light_blue_100))
    supportTeamContactButton.setTextColor(requireContext().resolveColor(R.color.simple_light_blue_500))
  }

  override fun openContactUrl(url: String) {
    val packageManager = requireContext().packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    if (intent.resolveActivity(packageManager) != null) {
      requireContext().startActivity(intent)
    } else {
      CrashReporter.report(ActivityNotFoundException("Unable to open contact url because no supporting apps were found."))
    }
  }

  override fun openSimpleInGooglePlay() {
    val packageManager = requireContext().packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL_FOR_SIMPLE))

    if (intent.resolveActivity(packageManager) != null) {
      requireContext().startActivity(intent)
    } else {
      CrashReporter.report(ActivityNotFoundException("Unable to play store url because no supporting apps were found."))
    }
  }

  @Parcelize
  data class Key(
      val appUpdateNudgePriority: AppUpdateNudgePriority,
      override val analyticsName: String = "Critical App Update Dialog",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment() = CriticalAppUpdateDialog()
  }

  interface Injector {
    fun inject(target: CriticalAppUpdateDialog)
  }
}
