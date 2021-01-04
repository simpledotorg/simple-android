package org.simple.clinic.login.pin

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenLoginPinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.security.pin.PinAuthenticated
import org.simple.clinic.security.pin.verification.LoginPinServerVerificationMethod.UserData
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class LoginPinScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), LoginPinScreenUi, UiActions, HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandler: LoginPinEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  private var binding: ScreenLoginPinBinding? = null

  private val pinEntryCardView
    get() = binding!!.pinEntryCardView

  private val backButton
    get() = binding!!.backButton

  private val phoneNumberTextView
    get() = binding!!.phoneNumberTextView

  private val hardwareBackClicks = PublishSubject.create<PinBackClicked>()

  private val events by unsafeLazy {
    Observable
        .mergeArray(
            pinAuthentications(),
            backClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = LoginPinUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = LoginPinModel.create(),
        init = LoginPinInit(),
        update = LoginPinUpdate(),
        effectHandler = effectHandler.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenLoginPinBinding.bind(this)

    context.injector<Injector>().inject(this)

    pinEntryCardView.setForgotButtonVisible(false)
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

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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
