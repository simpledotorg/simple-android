package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.view_teleconsult_doctor_info.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.User
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.textChanges
import javax.inject.Inject

class TeleconsultDoctorInfoView(
    context: Context,
    attr: AttributeSet?
) : ConstraintLayout(context, attr), TeleconsultDoctorInfoUi, TeleconsultDoctorInfoUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: TeleconsultDoctorInfoEffectHandler.Factory

  init {
    inflate(context, R.layout.view_teleconsult_doctor_info, this)
  }

  private val events by unsafeLazy {
    Observable
        .merge(
            instructionChanges(),
            medicalRegistrationIdChanges()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TeleconsultDoctorInfoUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = TeleconsultDoctorInfoModel.create(),
        init = TeleconsultDoctorInfoInit(),
        update = TeleconsultDoctorInfoUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
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

  override fun setMedicalRegistrationId(medicalRegistrationId: String) {
    medicalRegistrationIdEditText.setText(medicalRegistrationId)
  }

  override fun setSignatureBitmap(bitmap: Bitmap) {

  }

  override fun renderDoctorAcknowledgement(user: User) {
    val acknowledgementString = context.getString(R.string.view_teleconsult_doctor_info_acknowledgement, user.fullName)
    acknowledgementTextView.text = acknowledgementString
  }

  private fun instructionChanges(): Observable<UiEvent> {
    return instructionsEditText
        .textChanges { MedicalInstructionsChanged(it) }
  }

  private fun medicalRegistrationIdChanges(): Observable<UiEvent> {
    return medicalRegistrationIdEditText
        .textChanges { MedicalRegistrationIdChanged(it) }
  }

  interface Injector {
    fun inject(target: TeleconsultDoctorInfoView)
  }
}
