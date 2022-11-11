package org.simple.clinic.registration.facility

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenRegistrationFacilitySelectionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.introvideoscreen.IntroVideoScreen
import org.simple.clinic.navigation.v2.ActivityResult
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registration.confirmfacility.ConfirmFacilitySheet
import org.simple.clinic.registration.register.RegistrationLoadingScreen
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class RegistrationFacilitySelectionScreen : BaseScreen<
    RegistrationFacilitySelectionScreen.Key,
    ScreenRegistrationFacilitySelectionBinding,
    RegistrationFacilitySelectionModel,
    RegistrationFacilitySelectionEvent,
    RegistrationFacilitySelectionEffect,
    RegistrationFacilitySelectionViewEffect>(), RegistrationFacilitySelectionUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: RegistrationFacilitySelectionEffectHandler.Factory

  @Inject
  lateinit var screenResultBus: ScreenResultBus

  private val facilityPickerView
    get() = binding.facilityPickerView

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = ScreenRegistrationFacilitySelectionBinding
      .inflate(layoutInflater, container, false)

  override fun createInit() = RegistrationFacilitySelectionInit()

  override fun createUpdate() = RegistrationFacilitySelectionUpdate(showIntroVideoScreen = true)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<RegistrationFacilitySelectionViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer = viewEffectsConsumer)
      .build()

  override fun viewEffectHandler() = RegistrationFacilitySelectionViewEffectHandler(this)

  override fun defaultModel() = RegistrationFacilitySelectionModel.create(
      entry = screenKey.ongoingRegistrationEntry
  )

  override fun events() = Observable
      .mergeArray(
          facilityClicks(),
          registrationFacilityConfirmations()
      )
      .compose(ReportAnalyticsEvents())
      .cast<RegistrationFacilitySelectionEvent>()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    facilityPickerView.backClicked = { router.pop() }
  }

  private fun facilityClicks(): Observable<RegistrationFacilitySelectionEvent> {
    return Observable.create { emitter ->
      facilityPickerView.facilitySelectedCallback = { emitter.onNext(RegistrationFacilityClicked(it)) }
    }
  }

  private fun registrationFacilityConfirmations(): Observable<UiEvent> {
    return screenResultBus
        .streamResults()
        .ofType<ActivityResult>()
        .extractSuccessful(CONFIRM_FACILITY_SHEET) { intent ->
          val confirmedFacilityUuid = ConfirmFacilitySheet.confirmedFacilityUuid(intent)
          RegistrationFacilityConfirmed(confirmedFacilityUuid)
        }
  }

  override fun openIntroVideoScreen(registrationEntry: OngoingRegistrationEntry) {
    router.push(IntroVideoScreen.Key(registrationEntry))
  }

  override fun openRegistrationLoadingScreen(registrationEntry: OngoingRegistrationEntry) {
    router.push(RegistrationLoadingScreen.Key(registrationEntry))
  }

  override fun showConfirmFacilitySheet(facilityUuid: UUID, facilityName: String) {
    val intent = ConfirmFacilitySheet.intentForConfirmFacilitySheet(requireContext(), facilityUuid, facilityName)
    activity.startActivityForResult(intent, CONFIRM_FACILITY_SHEET)
  }

  companion object {
    private const val CONFIRM_FACILITY_SHEET = 1
  }

  interface Injector {
    fun inject(target: RegistrationFacilitySelectionScreen)
  }

  @Parcelize
  data class Key(
      val ongoingRegistrationEntry: OngoingRegistrationEntry,
      override val analyticsName: String = "Registration Facility Selection"
  ) : ScreenKey() {

    override fun instantiateFragment() = RegistrationFacilitySelectionScreen()
  }
}
