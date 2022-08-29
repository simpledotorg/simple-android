package org.simple.clinic.summary.assignedfacility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import io.reactivex.Observable
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

  private val assignedFacilityTextView
    get() = binding!!.assignedFacilityTextView

  private val changeAssignedFacilityButton
    get() = binding!!.changeAssignedFacilityButton

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
    changeAssignedFacilityButton.setOnClickListener {
      changeAssignedFacilityClicks?.invoke()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
    delegate.stop()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun renderAssignedFacilityName(facilityName: String) {
    assignedFacilityTextView.text = facilityName
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
