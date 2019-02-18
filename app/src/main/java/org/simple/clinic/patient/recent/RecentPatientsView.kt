package org.simple.clinic.patient.recent

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter

class RecentPatientsView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

  override fun onFinishInflate() {
    super.onFinishInflate()
    layoutManager = LinearLayoutManager(context)

    val groupAdapter = GroupAdapter<com.xwray.groupie.ViewHolder>()
    adapter = groupAdapter

    groupAdapter.add(RecentPatientItem())
    groupAdapter.add(RecentPatientItem())
    groupAdapter.add(RecentPatientItem())
    groupAdapter.add(RecentPatientItem())
    groupAdapter.add(RecentPatientItem())
    groupAdapter.add(RecentPatientItem())
    groupAdapter.add(RecentPatientItem())
  }
}
