package org.simple.clinic.teleconsultlog.prescription

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_teleconsult_prescription.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class TeleconsultPrescriptionScreen constructor(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs), TeleconsultPrescriptionUi, TeleconsultPrescriptionUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandler: TeleconsultPrescriptionEffectHandler.Factory

  @Inject
  lateinit var userClock: UserClock

  private val screenKey by unsafeLazy {
    screenRouter.key<TeleconsultPrescriptionScreenKey>(this)
  }

  private val events by unsafeLazy {
    backClicks()
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TeleconsultPrescriptionUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = TeleconsultPrescriptionModel.create(screenKey.patientUuid),
        init = TeleconsultPrescriptionInit(),
        update = TeleconsultPrescriptionUpdate(),
        effectHandler = effectHandler.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
    context.injector<Injector>().inject(this)
  }

  override fun renderPatientDetails(patient: Patient) {
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)
    val patientGender = patient.gender
    toolbar.title = context.getString(
        R.string.screen_teleconsult_prescription_patient_details,
        patient.fullName,
        context.getString(patientGender.displayLetterRes),
        ageValue.toString()
    )
  }

  override fun goBackToPreviousScreen() {
    screenRouter.pop()
  }

  override fun showSignatureRequiredError() {
    teleconsultPrescriptionDoctorInfoView.showSignatureError()
  }

  private fun backClicks(): Observable<UiEvent> {
    return toolbar
        .navigationClicks()
        .map { BackClicked }
  }

  interface Injector {
    fun inject(target: TeleconsultPrescriptionScreen)
  }
}
