package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_teleconsult_record.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessScreenKey
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.No
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Message
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Video
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class TeleconsultRecordScreen(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), TeleconsultRecordUi, UiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: TeleconsultRecordEffectHandler.Factory

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var activity: AppCompatActivity

  private val radioIdToTeleconsultationType = mapOf(
      R.id.teleconsultTypeAudioRadioButton to Audio,
      R.id.teleconsultTypeVideoRadioButton to Video,
      R.id.teleconsultTypeMessageRadioButton to Message
  )

  private val events by unsafeLazy {
    Observable
        .merge(
            doneClicks(),
            backClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val screenKey by unsafeLazy {
    screenRouter.key<TeleconsultRecordScreenKey>(this)
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TeleconsultRecordUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = TeleconsultRecordModel.create(
            patientUuid = screenKey.patientUuid,
            teleconsultRecordId = screenKey.teleconsultRecordId
        ),
        init = TeleconsultRecordInit(),
        update = TeleconsultRecordUpdate(),
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

  override fun renderPatientDetails(patient: Patient) {
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)
    val patientGender = patient.gender
    toolbar.title = context.getString(
        R.string.screen_teleconsult_record_patient_details,
        patient.fullName,
        context.getString(patientGender.displayLetterRes),
        ageValue.toString()
    )
  }

  override fun setTeleconsultationType(teleconsultationType: TeleconsultationType) {
    val id = when (teleconsultationType) {
      Audio -> R.id.teleconsultTypeAudioRadioButton
      Video -> R.id.teleconsultTypeVideoRadioButton
      Message -> R.id.teleconsultTypeMessageRadioButton
      is TeleconsultationType.Unknown -> -1
    }
    teleconsultTypeRadioGroup.check(id)
  }

  override fun setPatientTookMedicines(patientTookMedicines: Answer) {
    patientTookMedicineCheckBox.isChecked = booleanForAnswer(patientTookMedicines)
  }

  override fun setPatientConsented(patientConsented: Answer) {
    patientConsentedCheckBox.isChecked = booleanForAnswer(patientConsented)
  }

  override fun goBackToPreviousScreen() {
    screenRouter.pop()
  }

  override fun navigateToTeleconsultSuccessScreen() {
    screenRouter.push(TeleConsultSuccessScreenKey(
        patientUuid = screenKey.patientUuid,
        teleconsultRecordId = screenKey.teleconsultRecordId
    ))
  }

  override fun showTeleconsultNotRecordedWarning() {
    TeleconsultNotRecordedDialog.show(activity.supportFragmentManager)
  }

  override fun hideProgress() {
    doneButton.setButtonState(Enabled)
  }

  override fun showProgress() {
    doneButton.setButtonState(InProgress)
  }

  private fun doneClicks(): Observable<UiEvent> {
    return doneButton
        .clicks()
        .map {
          val teleconsultationType = radioIdToTeleconsultationType.getValue(teleconsultTypeRadioGroup.checkedRadioButtonId)
          val patientTookMedicines = answerForBoolean(patientTookMedicineCheckBox.isChecked)
          val patientConsented = answerForBoolean(patientConsentedCheckBox.isChecked)

          DoneClicked(
              teleconsultationType = teleconsultationType,
              patientTookMedicines = patientTookMedicines,
              patientConsented = patientConsented
          )
        }
  }

  private fun backClicks(): Observable<UiEvent> {
    val hardwareBackKeyClicks = Observable.create<Unit> { emitter ->
      val interceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(Unit)
          callback.markBackPressIntercepted()
        }
      }
      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(interceptor) }
      screenRouter.registerBackPressInterceptor(interceptor)
    }

    return toolbar
        .navigationClicks()
        .mergeWith(hardwareBackKeyClicks)
        .map { BackClicked }
  }

  private fun answerForBoolean(value: Boolean): Answer {
    return if (value)
      Yes
    else
      No
  }

  private fun booleanForAnswer(answer: Answer): Boolean {
    return when (answer) {
      Yes -> true
      No,
      is Answer.Unknown -> false
    }
  }

  interface Injector {
    fun inject(target: TeleconsultRecordScreen)
  }
}
