package org.simple.clinic.patient.recent

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientConfig
import javax.inject.Inject

class RecentPatientsView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

  @Inject
  lateinit var config: Observable<PatientConfig>

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    config.subscribeBy(onNext = {
      if (it.showRecentPatients) {
        initView()
      }
    })
  }

  private fun initView() {
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
