package org.simple.clinic.facility.alertchange

import android.app.Activity
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
import org.simple.clinic.facility.change.FacilityChangeActivity
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.router.screen.FullScreenKey
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class AlertFacilityChangeSheet : BaseBottomSheet<
    AlertFacilityChangeSheet.Key,
    SheetAlertFacilityChangeBinding,
    AlertFacilityChangeModel,
    AlertFacilityChangeEvent,
    AlertFacilityChangeEffect>() {

  @Inject
  lateinit var locale: Locale

  @Inject
  @Named("is_facility_switched")
  lateinit var isFacilitySwitchedPreference: Preference<Boolean>

  @Inject
  lateinit var features: Features

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

  companion object {
    const val FACILITY_CHANGE = 101
    private const val CURRENT_FACILITY_NAME = "current_facility"
    private const val CONTINUE_TO = "continue_to"

    private const val EXTRA_CONTINUE_TO = "extra_continue_to"

    fun intent(
        context: Context,
        currentFacilityName: String,
        continuation: Continuation
    ): Intent {
      val intent = Intent(context, AlertFacilityChangeSheet::class.java)
      intent.putExtra(CURRENT_FACILITY_NAME, currentFacilityName)
      intent.putExtra(CONTINUE_TO, continuation)
      return intent
    }

    fun <T : Parcelable> readContinuationExtra(intent: Intent): T {
      return intent.getParcelableExtra<T>(EXTRA_CONTINUE_TO)!!
    }
  }

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

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (isFacilitySwitchedPreference.get().not()) {
      closeSheetWithContinuation()
    } else {
      facilityName.text = getString(R.string.alertfacilitychange_facility_name, currentFacilityName)
      yesButton.setOnClickListener {
        closeSheetWithResult(Activity.RESULT_OK)
      }

      changeButton.setOnClickListener {
        openFacilityChangeScreen()
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == FACILITY_CHANGE) {
      closeSheetWithResult(resultCode)
    }
  }

  private fun closeSheetWithResult(resultCode: Int) {
    if (resultCode == Activity.RESULT_OK) {
      isFacilitySwitchedPreference.set(false)
      closeSheetWithContinuation()
    } else {
      val intent = Intent()
      setResult(resultCode, intent)
      finish()
    }
  }

  private fun closeSheetWithContinuation() {
    val intent = Intent()
    intent.putExtra(EXTRA_CONTINUE_TO, continuation)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  private fun openFacilityChangeScreen() {
    startActivityForResult(FacilityChangeActivity.intent(this), FACILITY_CHANGE)
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
}

sealed class Continuation : Parcelable {

  @Parcelize
  data class ContinueToScreen(val screenKey: FullScreenKey) : Continuation()

  @Parcelize
  data class ContinueToActivity(val intent: Intent, val requestCode: Int) : Continuation()
}
