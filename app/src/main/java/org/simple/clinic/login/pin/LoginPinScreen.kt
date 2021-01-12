package org.simple.clinic.login.pin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenLoginPinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.main.TheActivity
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.security.pin.PinAuthenticated
import org.simple.clinic.security.pin.verification.LoginPinServerVerificationMethod.UserData
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class LoginPinScreen :
    BaseScreen<
        LoginPinScreenKey,
        ScreenLoginPinBinding,
        LoginPinModel,
        LoginPinEvent,
        LoginPinEffect>(),
    LoginPinScreenUi,
    UiActions,
    HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandler: LoginPinEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  private val uiRenderer = LoginPinUiRenderer(this)

  private val pinEntryCardView
    get() = binding.pinEntryCardView

  private val backButton
    get() = binding.backButton

  private val phoneNumberTextView
    get() = binding.phoneNumberTextView

  private val hardwareBackClicks = PublishSubject.create<PinBackClicked>()

  override fun defaultModel() = LoginPinModel.create()

  override fun onModelUpdate(model: LoginPinModel) {
    uiRenderer.render(model)
  }

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenLoginPinBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(
          pinAuthentications(),
          backClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<LoginPinEvent>()

  override fun createUpdate() = LoginPinUpdate()

  override fun createInit() = LoginPinInit()

  override fun createEffectHandler() = effectHandler.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    pinEntryCardView.setForgotButtonVisible(false)
  }

  private fun pinAuthentications(): Observable<UiEvent> {
    return pinEntryCardView
        .downstreamUiEvents
        .ofType<PinAuthenticated>()
        .map { mapUserDataToLoginEntry(it.data as UserData) }
        .map(::LoginPinAuthenticated)
  }

  private fun mapUserDataToLoginEntry(userData: UserData): OngoingLoginEntry {
    return OngoingLoginEntry(
        uuid = userData.uuid,
        fullName = userData.fullName,
        phoneNumber = userData.phoneNumber,
        pin = userData.pin,
        pinDigest = userData.pinDigest,
        registrationFacilityUuid = userData.registrationFacilityUuid,
        status = userData.status,
        createdAt = userData.createdAt,
        updatedAt = userData.updatedAt,
        teleconsultPhoneNumber = userData.teleconsultPhoneNumber,
        capabilities = userData.capabilities
    )
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(PinBackClicked)
    return true
  }

  private fun backClicks(): Observable<PinBackClicked> {
    val backClicksFromView = backButton.clicks().map { PinBackClicked }

    return backClicksFromView.mergeWith(hardwareBackClicks)
  }

  override fun showPhoneNumber(phoneNumber: String) {
    phoneNumberTextView.text = phoneNumber
  }

  override fun openHomeScreen() {
    val intent = TheActivity
        .newIntent(activity, isFreshAuthentication = true)
        .disableAnimations()

    activity.startActivity(intent)
    activity.finishWithoutAnimations()
  }

  override fun goBackToRegistrationScreen() {
    router.pop()
  }

  interface Injector {
    fun inject(target: LoginPinScreen)
  }
}
