package org.simple.clinic.patient.recent

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import io.reactivex.Observable
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientConfig
import javax.inject.Inject

class RecentPatientsView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

  @Inject
  lateinit var config: Observable<PatientConfig>

  private val groupAdapter = GroupAdapter<com.xwray.groupie.ViewHolder>()

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    layoutManager = LinearLayoutManager(context)
    adapter = groupAdapter

    addDummyData()
  }

  private fun addDummyData() {
    groupAdapter.add(RecentPatientItem(
        name = "Anish Acharya",
        age = 43,
        lastBp = "140/90",
        gender = Gender.TRANSGENDER
    ))
    groupAdapter.add(RecentPatientItem(
        name = "Anish24 Acharya",
        age = 43,
        lastBp = "141/90",
        gender = Gender.MALE
    ))
    groupAdapter.add(RecentPatientItem(
        name = "Anish3 Acharya",
        age = 43,
        lastBp = "142/90",
        gender = Gender.FEMALE
    ))
    groupAdapter.add(RecentPatientItem(
        name = "Anish4 Acharya",
        age = 43,
        lastBp = "145/90",
        gender = Gender.TRANSGENDER
    ))
    groupAdapter.add(RecentPatientItem(
        name = "Anish55 Acharya",
        age = 43,
        lastBp = "149/90",
        gender = Gender.MALE
    ))
    groupAdapter.add(RecentPatientItem(
        name = "Anish6 Acharya",
        age = 43,
        lastBp = "141/90",
        gender = Gender.FEMALE
    ))
    groupAdapter.add(RecentPatientItem(
        name = "Anish7 Acharya",
        age = 43,
        lastBp = "142/90",
        gender = Gender.TRANSGENDER
    ))
  }
}
