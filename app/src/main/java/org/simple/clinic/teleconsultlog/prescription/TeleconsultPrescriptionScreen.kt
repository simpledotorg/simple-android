package org.simple.clinic.teleconsultlog.prescription

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenTeleconsultPrescriptionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.teleconsultlog.shareprescription.TeleconsultSharePrescriptionScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class TeleconsultPrescriptionScreen : BaseScreen<
    TeleconsultPrescriptionScreen.Key,
    ScreenTeleconsultPrescriptionBinding,
    TeleconsultPrescriptionModel,
    TeleconsultPrescriptionEvent,
    TeleconsultPrescriptionEffect,
    Unit>(), TeleconsultPrescriptionUi, TeleconsultPrescriptionUiActions {

  private val toolbar
    get() = binding.toolbar

  private val teleconsultPrescriptionDoctorInfoView
    get() = binding.teleconsultPrescriptionDoctorInfoView

  private val teleconsultPrescriptionMedicinesView
    get() = binding.teleconsultPrescriptionMedicinesView

  private val nextButton
    get() = binding.nextButton

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandler: TeleconsultPrescriptionEffectHandler.Factory

  @Inject
  lateinit var userClock: UserClock

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun defaultModel() = TeleconsultPrescriptionModel.create(
      screenKey.teleconsultRecordId,
      screenKey.patientUuid
  )

  override fun events() = Observable
      .merge(
          backClicks(),
          nextClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<TeleconsultPrescriptionEvent>()

  override fun createInit() = TeleconsultPrescriptionInit()

  override fun createUpdate() = TeleconsultPrescriptionUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandler
      .create(this)
      .build()

  override fun uiRenderer() = TeleconsultPrescriptionUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = ScreenTeleconsultPrescriptionBinding
      .inflate(layoutInflater, container, false)

  override fun renderPatientDetails(patient: Patient) {
    val ageValue = patient.ageDetails.estimateAge(userClock)
    val patientGender = patient.gender
    toolbar.title = getString(
        R.string.screen_teleconsult_prescription_patient_details,
        patient.fullName,
        getString(patientGender.displayLetterRes),
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

  @Parcelize
  data class Key(
      val patientUuid: UUID,
      val teleconsultRecordId: UUID,
      override val analyticsName: String = "Teleconsultation Prescription"
  ) : ScreenKey() {

    override fun instantiateFragment() = TeleconsultPrescriptionScreen()
  }

  interface Injector {
    fun inject(target: TeleconsultPrescriptionScreen)
  }
}
