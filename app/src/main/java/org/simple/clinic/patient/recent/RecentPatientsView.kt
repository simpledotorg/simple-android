package org.simple.clinic.patient.recent

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import javax.inject.Inject

class RecentPatientsView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  private val groupAdapter = GroupAdapter<com.xwray.groupie.ViewHolder>()

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    layoutManager = LinearLayoutManager(context)
    adapter = groupAdapter

    userSession.requireLoggedInUser()
        .takeUntil(RxView.detaches(this))
        .map(facilityRepository::currentFacilityUuid)
        .flatMap(patientRepository::recentPatients)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          groupAdapter.clear()
          groupAdapter.addAll(it.map {
            RecentPatientItem(
                name = it.fullName,
                age = it.age?.value ?: -1,
                lastBp = "${it.lastBp?.systolic}/${it.lastBp?.diastolic}",
                gender = it.gender
            )
          })
        }
  }
}
