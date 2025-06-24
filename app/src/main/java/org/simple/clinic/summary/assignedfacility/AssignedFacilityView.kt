package org.simple.clinic.summary.assignedfacility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.card.MaterialCardView
import io.reactivex.Observable
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.databinding.PatientsummaryAssignedFacilityContentBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.assignedfacility.ui.AssignedFacility
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

private typealias ChangeAssignedFacilityClicked = () -> Unit
private typealias AssignedFacilityChanges = () -> Unit

class AssignedFacilityView(
    context: Context,
    attrs: AttributeSet
) : MaterialCardView(context, attrs), AssignedFacilityUi, UiActions, PatientSummaryChildView {

  var changeAssignedFacilityClicks: ChangeAssignedFacilityClicked? = null
  var assignedFacilityChanges: AssignedFacilityChanges? = null

  private var binding: PatientsummaryAssignedFacilityContentBinding? = null

  private var assignedFacilityName by mutableStateOf("")

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = PatientsummaryAssignedFacilityContentBinding.inflate(layoutInflater, this, true)
  }

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: AssignedFacilityEffectHandler.Factory

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val externalEvents = DeferredEventSource<AssignedFacilityEvent>()

  private val delegate by unsafeLazy {
    val uiRenderer = AssignedFacilityUiRenderer(this)
    val patientUuid = screenKeyProvider.keyFor<PatientSummaryScreenKey>(this).patientUuid

    MobiusDelegate.forView(
        events = Observable.never(),
        defaultModel = AssignedFacilityModel.create(patientUuid = patientUuid),
        update = AssignedFacilityUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = AssignedFacilityInit(),
        additionalEventSources = listOf(externalEvents),
        modelUpdateListener = { model ->
          modelUpdateCallback?.invoke(model)
          uiRenderer.render(model)
        }
    )
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    addView(ComposeView(context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

      setContent {
        SimpleTheme {
          AssignedFacility(
              facilityName = assignedFacilityName
          ) {
            changeAssignedFacilityClicks?.invoke()
          }
        }
      }
    })
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    delegate.stop()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun renderAssignedFacilityName(facilityName: String) {
    assignedFacilityName = facilityName
  }

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }

  override fun notifyAssignedFacilityChanged() {
    assignedFacilityChanges?.invoke()
  }

  fun onNewAssignedFacilitySelected(facility: Facility) {
    externalEvents.notify(AssignedFacilitySelected(facility))
  }

  interface Injector {
    fun inject(target: AssignedFacilityView)
  }
}
