package org.simple.clinic.drugs.selectionv2

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import javax.inject.Inject

class PrescribedDrugWithDosagesSheet : BottomSheetActivity() {

  @Inject
  lateinit var adapter: PrescribedDosageAdapter

  private val recyclerView by bindView<RecyclerView>(R.id.prescribed_drug_with_dosages_list)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_prescribed_drug_with_dosages)
    TheActivity.component.inject(this)

    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)

  }
}
