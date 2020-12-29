package org.simple.clinic.registration.facility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenRegistrationFacilitySelectionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.introvideoscreen.IntroVideoScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.registration.confirmfacility.ConfirmFacilitySheet
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class RegistrationFacilitySelectionScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationFacilitySelectionUiActions {

  var binding: ScreenRegistrationFacilitySelectionBinding? = null

  private val facilityPickerView
    get() = binding!!.facilityPickerView

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: RegistrationFacilitySelectionEffectHandler.Factory

  private val events by unsafeLazy {
    Observable
        .mergeArray(
            facilityClicks(),
            registrationFacilityConfirmations()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val screenKey = screenKeyProvider.keyFor<RegistrationFacilitySelectionScreenKey>(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationFacilitySelectionModel.create(screenKey.ongoingRegistrationEntry),
        update = RegistrationFacilitySelectionUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = RegistrationFacilitySelectionInit()
    )
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    binding = ScreenRegistrationFacilitySelectionBinding.bind(this)
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    facilityPickerView.backClicked = { screenRouter.pop() }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    delegate.stop()
    binding = null
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun facilityClicks(): Observable<RegistrationFacilitySelectionEvent> {
    return Observable.create { emitter ->
      facilityPickerView.facilitySelectedCallback = { emitter.onNext(RegistrationFacilityClicked(it)) }
    }
  }

  private fun registrationFacilityConfirmations(): Observable<UiEvent> {
    return screenRouter
        .streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(CONFIRM_FACILITY_SHEET) { intent ->
          val confirmedFacilityUuid = ConfirmFacilitySheet.confirmedFacilityUuid(intent)
          RegistrationFacilityConfirmed(confirmedFacilityUuid)
        }
  }

  override fun openIntroVideoScreen(registrationEntry: OngoingRegistrationEntry) {
    screenRouter.push(IntroVideoScreenKey(registrationEntry))
  }

  override fun showConfirmFacilitySheet(facilityUuid: UUID, facilityName: String) {
    val intent = ConfirmFacilitySheet.intentForConfirmFacilitySheet(context, facilityUuid, facilityName)
    activity.startActivityForResult(intent, CONFIRM_FACILITY_SHEET)
  }

  companion object {
    private const val CONFIRM_FACILITY_SHEET = 1
  }

  interface Injector {
    fun inject(target: RegistrationFacilitySelectionScreen)
  }
}
