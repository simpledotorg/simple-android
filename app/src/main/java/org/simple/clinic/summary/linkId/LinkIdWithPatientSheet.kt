package org.simple.clinic.summary.linkId

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class LinkIdWithPatientSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_PATIENT_UUID = "patientUuid"
    private const val KEY_IDENTIFIER = "identifier"

    fun intent(context: Context, patientUuid: UUID, identifier: Identifier) {
      Intent(context, LinkIdWithPatientSheet::class.java)
          .putExtra(KEY_PATIENT_UUID, patientUuid)
          .putExtra(KEY_IDENTIFIER, identifier)
    }
  }

  @Inject
  lateinit var controller: LinkIdWithPatientSheetController

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

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
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val patientUuid = intent.extras.getSerializable(KEY_PATIENT_UUID) as UUID
    val identifier = intent.extras.getParcelable(KEY_IDENTIFIER) as Identifier

    return Observable.just(LinkIdWithPatientSheetCreated(patientUuid, identifier))
  }
}

