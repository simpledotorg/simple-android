package org.simple.clinic.facility.change

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenFacilityChangeBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.confirm.ConfirmFacilityChangeSheet
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class FacilityChangeScreen :
    BaseScreen<
        FacilityChangeScreen.Key,
        ScreenFacilityChangeBinding,
        FacilityChangeModel,
        FacilityChangeEvent,
        FacilityChangeEffect>(),
    FacilityChangeUi,
    FacilityChangeUiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var effectHandlerFactory: FacilityChangeEffectHandler.Factory

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  private val subscriptions = CompositeDisposable()

  override fun defaultModel() = FacilityChangeModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenFacilityChangeBinding.inflate(layoutInflater, container, false)

  override fun events() = facilityClicks()
      .compose(ReportAnalyticsEvents())
      .cast<FacilityChangeEvent>()

  override fun createInit() = FacilityChangeInit()

  override fun createUpdate() = FacilityChangeUpdate()

  override fun uiRenderer() = FacilityChangeUiRenderer(this)

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  private val facilityPickerView
    get() = binding.facilityPickerView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
    subscriptions.add(handleFacilityChangeConfirmation())
  }

  override fun onDetach() {
    subscriptions.clear()
    super.onDetach()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUiComponents()
  }

  private fun setupUiComponents() {
    facilityPickerView.backClicked = router::pop
  }

  private fun facilityClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      facilityPickerView.facilitySelectedCallback = { emitter.onNext(FacilityChangeClicked(it)) }
    }
  }

  private fun handleFacilityChangeConfirmation(): Disposable {
    return screenResults
        .streamResults()
        .ofType<ActivityResult>()
        .filter { it.requestCode == OPEN_CONFIRMATION_SHEET }
        .subscribe {
          if (it.resultCode == Activity.RESULT_OK)
            exitAfterChange()
          else
            goBack()
        }
  }

  private fun exitAfterChange() {
    router.popWithResult(Succeeded(FacilityChanged))
  }

  override fun goBack() {
    router.pop()
  }

  override fun openConfirmationSheet(facility: Facility) {
    startActivityForResult(
        ConfirmFacilityChangeSheet.intent(requireContext(), facility),
        OPEN_CONFIRMATION_SHEET
    )
  }

  @Parcelize
  class Key : ScreenKey() {

    override val analyticsName = "Change Facility"

    override fun instantiateFragment() = FacilityChangeScreen()
  }

  interface Injector {
    fun inject(target: FacilityChangeScreen)
  }

  @Parcelize
  object FacilityChanged : Parcelable

  companion object {
    private const val OPEN_CONFIRMATION_SHEET = 1210
  }
}
