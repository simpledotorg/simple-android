package org.simple.clinic.teleconsultlog.success

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import org.simple.clinic.R
import org.simple.clinic.databinding.ScreenTeleconsultSuccessBinding
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.teleconsultlog.prescription.TeleconsultPrescriptionScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import java.util.UUID
import javax.inject.Inject

class TeleConsultSuccessScreen(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet), TeleConsultSuccessScreenUiActions, TeleConsultSuccessUi {

  private var binding: ScreenTeleconsultSuccessBinding? = null

  private val prescriptionNoButton
    get() = binding!!.prescriptionNoButton

  private val prescriptionYesButton
    get() = binding!!.prescriptionYesButton

  private val toolbar
    get() = binding!!.toolbar

  @Inject
  lateinit var effectHandler: TeleConsultSuccessEffectHandler.Factory

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var userClock: UserClock

  private val events: Observable<TeleConsultSuccessEvent> by unsafeLazy {
    Observable
        .merge(
            yesClicks(),
            noClicks()
        )
  }

  private val uiRenderer = TeleConsultSuccessUiRenderer(this)

  private val mobiusDelegate: MobiusDelegate<TeleConsultSuccessModel, TeleConsultSuccessEvent, TeleConsultSuccessEffect> by unsafeLazy {
    val screenKey = screenRouter.key<TeleConsultSuccessScreenKey>(this)
    MobiusDelegate.forView(
        events = events,
        defaultModel = TeleConsultSuccessModel.create(screenKey.patientUuid, screenKey.teleconsultRecordId),
        init = TeleConsultSuccessInit(),
        update = TeleConsultSuccessUpdate(),
        effectHandler = effectHandler.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    binding = ScreenTeleconsultSuccessBinding.bind(this)
    context.injector<Injector>().inject(this)
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
      screenRouter.pop()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mobiusDelegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
    mobiusDelegate.stop()
  }

  override fun onSaveInstanceState(): Parcelable {
    return mobiusDelegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(mobiusDelegate.onRestoreInstanceState(state))
  }

  override fun goToHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey, direction = BACKWARD)
  }

  override fun goToPrescriptionScreen(patientUuid: UUID, teleconsultRecordId: UUID) {
    screenRouter.push(TeleconsultPrescriptionScreenKey(patientUuid = patientUuid, teleconsultRecordId = teleconsultRecordId))
  }

  override fun showPatientInfo(patient: Patient) {
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)
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

}
