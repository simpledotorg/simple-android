package org.simple.clinic.registration.register

import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenRegistrationLoadingBinding
import org.simple.clinic.di.injector
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class RegistrationLoadingScreen : BaseScreen<
    RegistrationLoadingScreen.Key,
    ScreenRegistrationLoadingBinding,
    RegistrationLoadingModel,
    RegistrationLoadingEvent,
    RegistrationLoadingEffect,
    Unit>(), RegistrationLoadingUi, RegistrationLoadingUiActions {

  var binding: ScreenRegistrationLoadingBinding? = null

  private val loaderBack
    get() = binding!!.loaderBack

  private val errorRetryButton
    get() = binding!!.errorRetryButton

  private val errorTitle
    get() = binding!!.errorTitle

  private val errorMessage
    get() = binding!!.errorMessage

  private val viewSwitcher
    get() = binding!!.viewSwitcher

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  lateinit var effectHandlerFactory: RegistrationLoadingEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  private val events by unsafeLazy {
    retryClicks()
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val screenKey = screenKeyProvider.keyFor<RegistrationLoadingScreenKey>(this)
    val uiRenderer = RegistrationLoadingUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationLoadingModel.create(screenKey.registrationEntry),
        effectHandler = effectHandlerFactory.create(this).build(),
        update = RegistrationLoadingUpdate(),
        init = RegistrationLoadingInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    binding = ScreenRegistrationLoadingBinding.bind(this)
    context.injector<Injector>().inject(this)

    loaderBack.setOnClickListener {
      router.pop()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun retryClicks() = errorRetryButton
      .clicks()
      .map { RegisterErrorRetryClicked }
      .doAfterNext { showLoader() }

  override fun openHomeScreen() {
    val intent = TheActivity
        .newIntent(activity, isFreshAuthentication = true)
        .disableAnimations()

    activity.startActivity(intent)
    activity.finishWithoutAnimations()
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
