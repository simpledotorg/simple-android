package org.simple.clinic.teleconsultlog.success

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.databinding.ScreenTeleconsultSuccessBinding
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.teleconsultlog.prescription.TeleconsultPrescriptionScreen
import org.simple.clinic.util.UserClock
import java.util.UUID
import javax.inject.Inject

class TeleConsultSuccessScreen : BaseScreen<
    TeleConsultSuccessScreen.Key,
    ScreenTeleconsultSuccessBinding,
    TeleConsultSuccessModel,
    TeleConsultSuccessEvent,
    TeleConsultSuccessEffect,
    Unit>(), TeleConsultSuccessScreenUiActions, TeleConsultSuccessUi {

  private val prescriptionNoButton
    get() = binding.prescriptionNoButton

  private val prescriptionYesButton
    get() = binding.prescriptionYesButton

  private val toolbar
    get() = binding.toolbar

  @Inject
  lateinit var effectHandler: TeleConsultSuccessEffectHandler.Factory

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userClock: UserClock

  override fun defaultModel() = TeleConsultSuccessModel.create(screenKey.patientUuid, screenKey.teleconsultRecordId)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenTeleconsultSuccessBinding.inflate(layoutInflater, container, false)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) =
      effectHandler.create(this).build()

  override fun createUpdate() = TeleConsultSuccessUpdate()

  override fun createInit() = TeleConsultSuccessInit()

  override fun events(): Observable<TeleConsultSuccessEvent> =
      Observable
          .merge(
              yesClicks(),
              noClicks()
          )

  override fun uiRenderer() = TeleConsultSuccessUiRenderer(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    backClicks()
  }

  private fun noClicks() = prescriptionNoButton
      .clicks()
      .map { NoPrescriptionClicked }

  private fun yesClicks() = prescriptionYesButton
      .clicks()
      .map { YesPrescriptionClicked }

  private fun backClicks() {
    toolbar.setNavigationOnClickListener {
      router.pop()
    }
  }

  override fun goToHomeScreen() {
    router.clearHistoryAndPush(HomeScreenKey)
  }

  override fun goToPrescriptionScreen(patientUuid: UUID, teleconsultRecordId: UUID) {
    router.push(TeleconsultPrescriptionScreen.Key(patientUuid = patientUuid, teleconsultRecordId = teleconsultRecordId))
  }

  override fun showPatientInfo(patient: Patient) {
    val ageValue = patient.ageDetails.estimateAge(userClock)
    val genderInitial: Gender = patient.gender
    toolbar.title = resources.getString(
        R.string.screen_teleconsult_success_patient_information,
        patient.fullName,
        resources.getString(genderInitial.displayLetterRes),
        ageValue.toString())
  }

  interface Injector {
    fun inject(target: TeleConsultSuccessScreen)
  }

  @Parcelize
  data class Key(
      val patientUuid: UUID,
      val teleconsultRecordId: UUID,
      override val analyticsName: String = "TeleConsultation Success"
  ) : ScreenKey() {
    override fun instantiateFragment(): Fragment = TeleConsultSuccessScreen()
  }
}
