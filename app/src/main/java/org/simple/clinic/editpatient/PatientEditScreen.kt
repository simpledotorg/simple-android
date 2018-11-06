package org.simple.clinic.editpatient

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.activity.TheActivityComponent
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.*
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class PatientEditScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  companion object {
    @JvmField
    val KEY = ::PatientEditScreenKey
  }

  @Inject
  lateinit var controller: PatientEditScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val fullNameEditText by bindView<EditText>(R.id.patientedit_full_name)
  private val phoneNumberEditText by bindView<EditText>(R.id.patientedit_phone_number)
  private val colonyEditText by bindView<EditText>(R.id.patientedit_colony_or_village)
  private val districtEditText by bindView<EditText>(R.id.patientedit_district)
  private val stateEditText by bindView<EditText>(R.id.patientedit_state)
  private val femaleRadioButton by bindView<RadioButton>(R.id.patientedit_gender_female)
  private val maleRadioButton by bindView<RadioButton>(R.id.patientedit_gender_male)
  private val transgenderRadioButton by bindView<RadioButton>(R.id.patientedit_gender_transgender)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val key = screenRouter.key<PatientEditScreenKey>(this)

    return Observable.just(PatientEditScreenCreated(key.patientUuid))
  }

  fun setPatientName(name: String) {
    fullNameEditText.setText(name)
  }

  fun setPatientPhoneNumber(number: String) {
    phoneNumberEditText.setText(number)
  }

  fun setColonyOrVillage(colonyOrVillage: String) {
    colonyEditText.setText(colonyOrVillage)
  }

  fun setDistrict(district: String) {
    districtEditText.setText(district)
  }

  fun setState(state: String) {
    stateEditText.setText(state)
  }

  fun setGender(gender: Gender) {
    val genderButton = when (gender) {
      MALE -> maleRadioButton
      FEMALE -> femaleRadioButton
      TRANSGENDER -> transgenderRadioButton
    }

    genderButton.isChecked = true
  }

  fun showValidationErrors(errors: Set<PatientEditValidationError>) {
    TODO()
  }
}
