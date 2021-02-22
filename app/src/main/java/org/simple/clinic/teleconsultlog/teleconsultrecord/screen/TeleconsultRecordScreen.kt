package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenTeleconsultRecordBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
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

class TeleconsultRecordScreen :
    BaseScreen<
        TeleconsultRecordScreenKey,
        ScreenTeleconsultRecordBinding,
        TeleconsultRecordModel,
        TeleconsultRecordEvent,
        TeleconsultRecordEffect>(),
    TeleconsultRecordUi,
    UiActions,
    HandlesBack {

  private var binding: ScreenTeleconsultRecordBinding? = null

  private val toolbar
    get() = binding!!.toolbar

  private val teleconsultTypeRadioGroup
    get() = binding!!.teleconsultTypeRadioGroup

  private val patientTookMedicineCheckBox
    get() = binding!!.patientTookMedicineCheckBox

  private val doneButton
    get() = binding!!.doneButton

  private val patientConsentedCheckBox
    get() = binding!!.patientConsentedCheckBox

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: TeleconsultRecordEffectHandler.Factory

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val radioIdToTeleconsultationType = mapOf(
      R.id.teleconsultTypeAudioRadioButton to Audio,
      R.id.teleconsultTypeVideoRadioButton to Video,
      R.id.teleconsultTypeMessageRadioButton to Message
  )

  private val hardwareBackClicks = PublishSubject.create<BackClicked>()

  private val events by unsafeLazy {
    Observable
        .merge(
            doneClicks(),
            backClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<TeleconsultRecordScreenKey>(this)
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

    binding = ScreenTeleconsultRecordBinding.bind(this)

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
    router.pop()
  }

  override fun navigateToTeleconsultSuccessScreen() {
    router.push(TeleConsultSuccessScreenKey(
        patientUuid = screenKey.patientUuid,
        teleconsultRecordId = screenKey.teleconsultRecordId
    ).wrap())
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

  private fun backClicks(): Observable<TeleconsultRecordEvent> {
    val toolbarBackClicks = toolbar
        .navigationClicks()
        .map { BackClicked }

    return toolbarBackClicks
        .mergeWith(hardwareBackClicks)
        .cast()
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(BackClicked)
    return true
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
