package org.simple.clinic.patient.recent

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import org.simple.clinic.patient.Gender

class RecentPatientsView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

  override fun onFinishInflate() {
    super.onFinishInflate()
    layoutManager = LinearLayoutManager(context)

    val groupAdapter = GroupAdapter<com.xwray.groupie.ViewHolder>()
    adapter = groupAdapter

    groupAdapter.add(RecentPatientItem(RecentPatientItem.Data(
        title = "Anish Acharya, 43",
        lastBp = "140/90",
        gender = Gender.TRANSGENDER
    )))
    groupAdapter.add(RecentPatientItem(RecentPatientItem.Data(
        title = "Anish24 Acharya, 43",
        lastBp = "141/90",
        gender = Gender.MALE
    )))
    groupAdapter.add(RecentPatientItem(RecentPatientItem.Data(
        title = "Anish3 Acharya, 43",
        lastBp = "142/90",
        gender = Gender.FEMALE
    )))
    groupAdapter.add(RecentPatientItem(RecentPatientItem.Data(
        title = "Anish4 Acharya, 43",
        lastBp = "145/90",
        gender = Gender.TRANSGENDER
    )))
    groupAdapter.add(RecentPatientItem(RecentPatientItem.Data(
        title = "Anish55 Acharya, 43",
        lastBp = "149/90",
        gender = Gender.MALE
    )))
    groupAdapter.add(RecentPatientItem(RecentPatientItem.Data(
        title = "Anish6 Acharya, 43",
        lastBp = "141/90",
        gender = Gender.FEMALE
    )))
    groupAdapter.add(RecentPatientItem(RecentPatientItem.Data(
        title = "Anish7 Acharya, 43",
        lastBp = "142/90",
        gender = Gender.TRANSGENDER
    )))
  }
}
