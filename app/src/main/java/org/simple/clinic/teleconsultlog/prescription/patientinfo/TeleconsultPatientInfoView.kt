package org.simple.clinic.teleconsultlog.prescription.patientinfo

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.reactivex.Observable
import org.simple.clinic.R
import org.simple.clinic.databinding.ViewTeleconsultPatientInfoBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.teleconsultlog.prescription.TeleconsultPrescriptionScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class TeleconsultPatientInfoView constructor(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), TeleconsultPatientInfoUi {

  private var binding: ViewTeleconsultPatientInfoBinding? = null

  private val patientAddressTextView
    get() = binding!!.patientAddressTextView

  private val patientNameTextView
    get() = binding!!.patientNameTextView

  private val prescriptionDateTextView
    get() = binding!!.prescriptionDateTextView

  @Inject
  lateinit var userClock: UserClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandler: TeleconsultPatientInfoEffectHandler

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val delegate by unsafeLazy {
    val screenKey = screenKeyProvider.keyFor<TeleconsultPrescriptionScreenKey>(this)
    val uiRenderer = TeleconsultPatientInfoUiRenderer(this)

    MobiusDelegate.forView(
        events = Observable.never(),
        defaultModel = TeleconsultPatientInfoModel.create(
            patientUuid = screenKey.patientUuid,
            prescriptionDate = LocalDate.now(userClock)
        ),
        init = TeleconsultPatientInfoInit(),
        update = TeleconsultPatientInfoUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  init {
    var layoutInflater = LayoutInflater.from(context)
    binding = ViewTeleconsultPatientInfoBinding.inflate(layoutInflater, this, true)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
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

  override fun renderPatientInformation(patientProfile: PatientProfile) {
    displayPatientNameGenderAge(patientProfile.patient)
    displayPatientAddress(patientProfile.address)
  }

  private fun displayPatientNameGenderAge(patient: Patient) {
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)
    val patientGender = patient.gender

    patientNameTextView.text = context.getString(
        R.string.view_teleconsult_patient_info,
        patient.fullName,
        context.getString(patientGender.displayLetterRes),
        ageValue.toString()
    )
  }

  private fun displayPatientAddress(address: PatientAddress) {
    patientAddressTextView.text = address.completeAddress
  }

  override fun renderPrescriptionDate(prescriptionDate: LocalDate) {
    prescriptionDateTextView.text = dateFormatter.format(prescriptionDate)
  }

  interface Injector {
    fun inject(target: TeleconsultPatientInfoView)
  }
}
