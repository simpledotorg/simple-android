package org.simple.clinic.summary.linkId

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Truss
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class LinkIdWithPatientSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_PATIENT_UUID = "patientUuid"
    private const val KEY_IDENTIFIER = "identifier"

    fun intent(context: Context, patientUuid: UUID, identifier: Identifier): Intent {
      return Intent(context, LinkIdWithPatientSheet::class.java)
          .putExtra(KEY_PATIENT_UUID, patientUuid)
          .putExtra(KEY_IDENTIFIER, identifier)
    }
  }

  @Inject
  lateinit var controller: LinkIdWithPatientSheetController

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  private val identifier by unsafeLazy {
    intent.extras?.getParcelable(KEY_IDENTIFIER) as Identifier
  }

  private val idTextView by bindView<TextView>(R.id.linkidwithpatient_text)
  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_link_id_with_patient)
    TheActivity.component.inject(this)

    Observable.merge(sheetCreates(), onDestroys)
        .observeOn(io())
        .compose(controller)
        .subscribeOn(mainThread())
        .takeUntil(onDestroys)
        .subscribe { uiChange -> uiChange(this) }

    renderIdentifierText()
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val patientUuid = intent.extras?.getSerializable(KEY_PATIENT_UUID) as UUID
    return Observable.just(LinkIdWithPatientSheetCreated(patientUuid, identifier))
  }

  private fun renderIdentifierText() {
    val identifierType = identifierDisplayAdapter.typeAsText(identifier)
    val identifierValue = identifierDisplayAdapter.valueAsText(identifier)

    val identifierTextAppearanceSpan = TextAppearanceSpan(applicationContext, R.style.Clinic_V2_TextAppearance_Body0Left_NumericBold_Grey0)

    idTextView.text = Truss()
        .append(resources.getString(R.string.linkidwithpatient_add_id_text, identifierType))
        .pushSpan(identifierTextAppearanceSpan)
        .append(identifierValue)
        .popSpan()
        .append(resources.getString(R.string.linkidwithpatient_to_patient_text))
        .build()
  }

  fun closeSheet() {
    finish()
  }
}
