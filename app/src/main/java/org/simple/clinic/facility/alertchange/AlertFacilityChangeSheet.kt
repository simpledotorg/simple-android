package org.simple.clinic.facility.alertchange

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.f2prateek.rx.preferences2.Preference
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.databinding.SheetAlertFacilityChangeBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.alertchange.Continuation.ContinueToActivity
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen_Old
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.ExpectsResult
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResult
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.util.resolveFloat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class AlertFacilityChangeSheet :
    BaseBottomSheet<
        AlertFacilityChangeSheet.Key,
        SheetAlertFacilityChangeBinding,
        AlertFacilityChangeModel,
        AlertFacilityChangeEvent,
        AlertFacilityChangeEffect>(),
    ExpectsResult {

  @Inject
  lateinit var locale: Locale

  @Inject
  @Named("is_facility_switched")
  lateinit var isFacilitySwitchedPreference: Preference<Boolean>

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var router: Router

  override fun defaultModel() = AlertFacilityChangeModel()

  override fun uiRenderer(): ViewRenderer<AlertFacilityChangeModel> {
    return object : ViewRenderer<AlertFacilityChangeModel> {
      override fun render(model: AlertFacilityChangeModel) {
        // Nothing to render here
      }
    }
  }

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
      SheetAlertFacilityChangeBinding.inflate(inflater, container, false)

  private val currentFacilityName
    get() = screenKey.currentFacilityName

  private val continuation
    get() = screenKey.continuation

  private val facilityName
    get() = binding.facilityName

  private val yesButton
    get() = binding.yesButton

  private val changeButton
    get() = binding.changeButton

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return super.onCreateDialog(savedInstanceState).apply {
      window!!.setDimAmount(0F)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (isFacilitySwitchedPreference.get().not()) {
      view.postDelayed(::closeSheetWithContinuation, 100)
    } else {
      showDialogUi()

      facilityName.text = getString(R.string.alertfacilitychange_facility_name, currentFacilityName)
      yesButton.setOnClickListener {
        proceedToNextScreen()
      }

      changeButton.setOnClickListener {
        openFacilityChangeScreen()
      }
    }
  }

  private fun showDialogUi() {
    val backgroundDimAmount = requireContext().resolveFloat(android.R.attr.backgroundDimAmount)
    requireDialog().window!!.setDimAmount(backgroundDimAmount)
    binding.root.visibility = View.VISIBLE
  }

  override fun onScreenResult(requestType: Parcelable, result: ScreenResult) {
    if (requestType == ChangeCurrentFacility && result is Succeeded) {
      proceedToNextScreen()
    }
  }

  private fun proceedToNextScreen() {
    isFacilitySwitchedPreference.set(false)
    closeSheetWithContinuation()
  }

  private fun closeSheetWithContinuation() {
    when (continuation) {
      is ContinueToScreen_Old -> {
        val screenKey = (continuation as ContinueToScreen_Old).screenKey
        router.replaceTop(screenKey.wrap())
      }
      is ContinueToActivity -> {
        val (intent, requestCode) = (continuation as ContinueToActivity).run {
          intent to requestCode
        }
        requireActivity().startActivityForResult(intent, requestCode)
        router.pop()
      }
      is Continuation.ContinueToScreen -> {
        val screenKey = (continuation as Continuation.ContinueToScreen).screenKey
        router.replaceTop(screenKey)
      }
      is Continuation.ContinueToScreenExpectingResult -> {
        val screenKey = (continuation as Continuation.ContinueToScreenExpectingResult).screenKey
        val requestType = (continuation as Continuation.ContinueToScreenExpectingResult).requestType

        router.replaceTopExpectingResult(requestType, screenKey)
      }
    }
  }

  private fun openFacilityChangeScreen() {
    router.pushExpectingResult(ChangeCurrentFacility, FacilityChangeScreen.Key())
  }

  @Parcelize
  data class Key(
      val currentFacilityName: String,
      val continuation: Continuation
  ) : ScreenKey() {

    override val analyticsName = "Alert Facility Change"

    override val type = ScreenType.Modal

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
  data class ContinueToScreen_Old(val screenKey: FullScreenKey) : Continuation()

  @Parcelize
  data class ContinueToScreen(val screenKey: ScreenKey) : Continuation()

  @Parcelize
  data class ContinueToScreenExpectingResult(val requestType: Parcelable, val screenKey: ScreenKey) : Continuation()

  @Parcelize
  data class ContinueToActivity(val intent: Intent, val requestCode: Int) : Continuation()
}
