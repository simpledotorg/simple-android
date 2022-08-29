package org.simple.clinic.registration.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenRegistrationLoadingBinding
import org.simple.clinic.di.injector
import org.simple.clinic.main.TheActivity
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import javax.inject.Inject

class RegistrationLoadingScreen : BaseScreen<
    RegistrationLoadingScreen.Key,
    ScreenRegistrationLoadingBinding,
    RegistrationLoadingModel,
    RegistrationLoadingEvent,
    RegistrationLoadingEffect,
    RegistrationLoadingViewEffect>(), RegistrationLoadingUi, RegistrationLoadingUiActions {

  private val loaderBack
    get() = binding.loaderBack

  private val errorRetryButton
    get() = binding.errorRetryButton

  private val errorTitle
    get() = binding.errorTitle

  private val errorMessage
    get() = binding.errorMessage

  private val viewSwitcher
    get() = binding.root

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: RegistrationLoadingEffectHandler.Factory

  override fun events() = retryClicks()
      .compose(ReportAnalyticsEvents())
      .cast<RegistrationLoadingEvent>()

  override fun defaultModel() = RegistrationLoadingModel.create(screenKey.registrationEntry)

  override fun createInit() = RegistrationLoadingInit()

  override fun createUpdate() = RegistrationLoadingUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<RegistrationLoadingViewEffect>) = effectHandlerFactory
      .create(
          viewEffectsConsumer = viewEffectsConsumer
      )
      .build()

  override fun uiRenderer() = RegistrationLoadingUiRenderer(this)

  override fun viewEffectHandler() = RegistrationLoadingViewEffectHandler(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = ScreenRegistrationLoadingBinding
      .inflate(layoutInflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    loaderBack.setOnClickListener {
      router.pop()
    }
  }

  private fun retryClicks() = errorRetryButton
      .clicks()
      .map { RegisterErrorRetryClicked }
      .doAfterNext { showLoader() }

  override fun openHomeScreen() {
    val intent = TheActivity
        .newIntent(requireContext(), isFreshAuthentication = true)
        .disableAnimations()

    requireActivity().startActivity(intent)
    requireActivity().finishWithoutAnimations()
  }

  override fun showNetworkError() {
    errorTitle.text = resources.getString(R.string.registrationloader_error_internet_connection_title)
    errorMessage.visibility = View.GONE
    viewSwitcher.showNext()
  }

  override fun showUnexpectedError() {
    errorTitle.text = resources.getString(R.string.registrationloader_error_unexpected_title)
    errorMessage.text = resources.getString(R.string.registrationloader_error_unexpected_message)
    errorMessage.visibility = View.VISIBLE
    viewSwitcher.showNext()
  }

  private fun showLoader() {
    viewSwitcher.showNext()
  }

  interface Injector {
    fun inject(target: RegistrationLoadingScreen)
  }

  @Parcelize
  data class Key(
      val registrationEntry: OngoingRegistrationEntry,
      override val analyticsName: String = "Ongoing Registration"
  ) : ScreenKey() {

    override fun instantiateFragment() = RegistrationLoadingScreen()
  }
}
