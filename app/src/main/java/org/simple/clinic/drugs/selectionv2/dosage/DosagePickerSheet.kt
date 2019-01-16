package org.simple.clinic.drugs.selectionv2.dosage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class DosagePickerSheet : BottomSheetActivity() {

  @Inject
  lateinit var adapter: DosageAdapter

  @Inject
  lateinit var controller: DosagePickerSheetController

  private val recyclerView by bindView<RecyclerView>(R.id.sheet_dosage_name_list)
  private val drugNameTextView by bindView<TextView>(R.id.sheet_dosage_drug_name)

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_dosage_picker)
    TheActivity.component.inject(this)

    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)
    displayDrugName()

    Observable.mergeArray(sheetCreates())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(onDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME)
    return Observable.just(DosagePickerSheetCreated(drugName))
  }

  private fun displayDrugName() {
    val drugName = intent.getStringExtra(KEY_DRUG_NAME)
    drugNameTextView.text = getString(R.string.prescribed_drug_with_dosages_sheet_drug_name, drugName)
  }

  fun populateDosageList(list: List<DosageListItem>) {
    adapter.submitList(list)
  }

  companion object {
    private const val KEY_DRUG_NAME = "drugName"

    fun intent(context: Context, drugName: String): Intent {
      val intent = Intent(context, DosagePickerSheet::class.java)
      intent.putExtra(KEY_DRUG_NAME, drugName)
      return intent
    }
  }
}
