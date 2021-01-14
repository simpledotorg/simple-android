package org.simple.clinic.summary.assignedfacility

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.PatientsummaryAssignedFacilityContentBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionActivity
import org.simple.clinic.summary.ASSIGNED_FACILITY_SELECTION
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class AssignedFacilityView(
    context: Context,
    attrs: AttributeSet
) : MaterialCardView(context, attrs), AssignedFacilityUi, UiActions, PatientSummaryChildView {

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
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var effectHandlerFactory: AssignedFacilityEffectHandler.Factory

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val events by unsafeLazy {
    Observable
        .merge(
            changeButtonClicks(),
            assignedFacilitySelected()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = AssignedFacilityUiRenderer(this)
    val patientUuid = screenKeyProvider.keyFor<PatientSummaryScreenKey>(this).patientUuid

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = AssignedFacilityModel.create(patientUuid = patientUuid),
        update = AssignedFacilityUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = AssignedFacilityInit(),
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

  override fun openFacilitySelection() {
    activity.startActivityForResult(Intent(context, FacilitySelectionActivity::class.java), ASSIGNED_FACILITY_SELECTION)
  }

  override fun renderAssignedFacilityName(facilityName: String) {
    assignedFacilityTextView.text = facilityName
  }

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }

  private fun changeButtonClicks(): Observable<AssignedFacilityEvent> {
    return changeAssignedFacilityButton.clicks().map { ChangeAssignedFacilityButtonClicked }
  }

  private fun assignedFacilitySelected(): Observable<AssignedFacilityEvent> {
    return screenResults
        .streamResults()
        .ofType<ActivityResult>()
        .extractSuccessful(ASSIGNED_FACILITY_SELECTION, FacilitySelectionActivity.Companion::selectedFacility)
        .map(::AssignedFacilitySelected)
  }

  interface Injector {
    fun inject(target: AssignedFacilityView)
  }
}
