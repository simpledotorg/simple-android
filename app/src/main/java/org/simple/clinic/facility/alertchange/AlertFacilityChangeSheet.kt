package org.simple.clinic.facility.alertchange

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetAlertFacilityChangeBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.FacilityChanged
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.YesButtonClicked
import org.simple.clinic.facility.alertchange.Continuation.ContinueToActivity
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.util.resolveFloat
import org.simple.clinic.util.setFragmentResultListener
import java.util.Locale
import javax.inject.Inject

class AlertFacilityChangeSheet : BaseBottomSheet<
    AlertFacilityChangeSheet.Key,
    SheetAlertFacilityChangeBinding,
    AlertFacilityChangeModel,
    AlertFacilityChangeEvent,
    AlertFacilityChangeEffect,
    AlertFacilityChangeViewEffect>(), AlertFacilityChangeUi, UiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: AlertFacilityChangeEffectHandler.Factory

  private val currentFacilityName
    get() = screenKey.currentFacilityName

  private val rootView
    get() = binding.root

  private val facilityName
    get() = binding.facilityName

  private val yesButton
    get() = binding.yesButton

  private val changeButton
    get() = binding.changeButton

  private val hotEvents = PublishSubject.create<AlertFacilityChangeEvent>()

  override fun defaultModel() = AlertFacilityChangeModel.default()

  override fun events(): Observable<AlertFacilityChangeEvent> = Observable.mergeArray(
      yesButtonClicks(),
      hotEvents
  )
      .compose(ReportAnalyticsEvents())
      .cast()

  override fun createInit() = AlertFacilityChangeInit()

  override fun createUpdate() = AlertFacilityChangeUpdate()

  override fun uiRenderer() = AlertFacilityChangeUiRenderer(this)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<AlertFacilityChangeViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer)
      .build()

  override fun viewEffectsHandler() = AlertFacilityChangeViewEffectHandler(this)

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
      SheetAlertFacilityChangeBinding.inflate(inflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setFragmentResultListener(ChangeCurrentFacility) { _, result ->
      if (result is Succeeded) hotEvents.onNext(FacilityChanged)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return super.onCreateDialog(savedInstanceState).apply {
      window!!.setDimAmount(0F)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    facilityName.text = getString(R.string.alertfacilitychange_facility_name, currentFacilityName)

    changeButton.setOnClickListener {
      openFacilityChangeScreen()
    }
  }

  override fun showFacilityChangeAlert() {
    val backgroundDimAmount = requireContext().resolveFloat(android.R.attr.backgroundDimAmount)
    requireDialog().window!!.setDimAmount(backgroundDimAmount)
    rootView.visibility = View.VISIBLE
  }

  override fun hideFacilityChangeAlert() {
    rootView.visibility = View.GONE
  }

  override fun closeSheetWithContinuation() {
    when (val continuation = screenKey.continuation) {
      is ContinueToActivity -> {
        val (intent, requestCode) = continuation.run {
          intent to requestCode
        }
        requireActivity().startActivityForResult(intent, requestCode)
        router.pop()
      }

      is Continuation.ContinueToScreen -> {
        router.replaceTop(continuation.screenKey)
      }

      is Continuation.ContinueToScreenExpectingResult -> {
        val screenKey = continuation.screenKey
        val requestType = continuation.requestType

        router.replaceTopExpectingResult(requestType, screenKey)
      }
    }
  }

  private fun yesButtonClicks() = yesButton
      .clicks()
      .map { YesButtonClicked }

  private fun openFacilityChangeScreen() {
    router.pushExpectingResult(ChangeCurrentFacility, FacilityChangeScreen.Key())
  }

  @Parcelize
  data class Key(
      val currentFacilityName: String,
      val continuation: Continuation,
      override val analyticsName: String = "Alert Facility Change",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment() = AlertFacilityChangeSheet()
  }

  interface Injector {
    fun inject(target: AlertFacilityChangeSheet)
  }

  @Parcelize
  object ChangeCurrentFacility : Parcelable
}

sealed class Continuation : Parcelable {

  @Parcelize
  data class ContinueToScreen(val screenKey: ScreenKey) : Continuation()

  @Parcelize
  data class ContinueToScreenExpectingResult(
      val requestType: Parcelable,
      val screenKey: ScreenKey
  ) : Continuation()

  @Parcelize
  data class ContinueToActivity(val intent: Intent, val requestCode: Int) : Continuation()
}
