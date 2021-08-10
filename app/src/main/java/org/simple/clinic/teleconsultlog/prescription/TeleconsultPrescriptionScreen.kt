package org.simple.clinic.teleconsultlog.prescription

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenTeleconsultPrescriptionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.teleconsultlog.shareprescription.TeleconsultSharePrescriptionScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.util.UUID
import javax.inject.Inject

class TeleconsultPrescriptionScreen constructor(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs), TeleconsultPrescriptionUi, TeleconsultPrescriptionUiActions {

  private var binding: ScreenTeleconsultPrescriptionBinding? = null

  private val toolbar
    get() = binding!!.toolbar

  private val teleconsultPrescriptionDoctorInfoView
    get() = binding!!.teleconsultPrescriptionDoctorInfoView

  private val teleconsultPrescriptionMedicinesView
    get() = binding!!.teleconsultPrescriptionMedicinesView

  private val nextButton
    get() = binding!!.nextButton

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandler: TeleconsultPrescriptionEffectHandler.Factory

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<TeleconsultPrescriptionScreenKey>(this)
  }

  private val events by unsafeLazy {
    Observable
        .merge(
            backClicks(),
            nextClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TeleconsultPrescriptionUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = TeleconsultPrescriptionModel.create(screenKey.teleconsultRecordId, screenKey.patientUuid),
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
    hideKeyboard()
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return

    binding = ScreenTeleconsultPrescriptionBinding.bind(this)
    context.injector<Injector>().inject(this)
  }

  override fun renderPatientDetails(patient: Patient) {
    val ageValue = patient.ageDetails.estimateAge(userClock)
    val patientGender = patient.gender
    toolbar.title = context.getString(
        R.string.screen_teleconsult_prescription_patient_details,
        patient.fullName,
        context.getString(patientGender.displayLetterRes),
        ageValue.toString()
    )
  }

  override fun goBackToPreviousScreen() {
    router.pop()
  }

  override fun showSignatureRequiredError() {
    teleconsultPrescriptionDoctorInfoView.showSignatureError()
  }

  override fun showMedicinesRequiredError() {
    teleconsultPrescriptionMedicinesView.showMedicinesRequiredError()
  }

  override fun openSharePrescriptionScreen(patientUuid: UUID, medicalInstructions: String) {
    router.push(TeleconsultSharePrescriptionScreenKey(patientUuid, medicalInstructions))
  }

  private fun backClicks(): Observable<UiEvent> {
    return toolbar
        .navigationClicks()
        .map { BackClicked }
  }

  private fun nextClicks(): Observable<UiEvent> {
    return nextButton
        .clicks()
        .map {
          val medicalInstructions = teleconsultPrescriptionDoctorInfoView.medicalInstructions
          val medicalRegistrationId = teleconsultPrescriptionDoctorInfoView.medicalRegistrationId

          NextButtonClicked(
              medicalInstructions = medicalInstructions,
              medicalRegistrationId = medicalRegistrationId
          )
        }
  }

  interface Injector {
    fun inject(target: TeleconsultPrescriptionScreen)
  }
}
