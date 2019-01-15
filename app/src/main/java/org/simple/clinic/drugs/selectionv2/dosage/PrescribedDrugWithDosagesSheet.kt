package org.simple.clinic.drugs.selectionv2.dosage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class PrescribedDrugWithDosagesSheet : BottomSheetActivity() {

  @Inject
  lateinit var adapter: PrescribedDosageAdapter

  @Inject
  lateinit var controller: PrescribedDrugWithDosagesSheetController

  private val recyclerView by bindView<RecyclerView>(R.id.prescribed_drug_with_dosages_list)
  private val drugNameTextView by bindView<TextView>(R.id.prescribed_drug_with_dosages_drug_name)

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.prescribed_drug_with_dosages_sheet)
    TheActivity.component.inject(this)

    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)
    displayDrugName()

    Observable.mergeArray(sheetCreates(), adapter.itemClicks)
        .observeOn(io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(onDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed)
    super.onDestroy()
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME)
    val patientUuid = intent.getSerializableExtra(KEY_PATIENT_UUID) as UUID
    return Observable.just(PrescribedDrugsWithDosagesSheetCreated(drugName, patientUuid))
  }

  private fun displayDrugName() {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME)
    if (drugName.isNullOrEmpty().not()) {
      drugNameTextView.text = getString(R.string.prescribed_drug_with_dosages_sheet_drug_name, drugName)
    }
  }

  fun populateDosageList(list: List<PrescribedDosageListItem>) {
    adapter.submitList(list)
  }

  companion object {
    private const val KEY_DRUG_NAME = "drugName"
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun intent(context: Context, drugName: String, patientUuid: UUID): Intent {
      val intent = Intent(context, PrescribedDrugWithDosagesSheet::class.java)
      intent.putExtra(KEY_DRUG_NAME, drugName)
      intent.putExtra(KEY_PATIENT_UUID, patientUuid)
      return intent
    }
  }
}
