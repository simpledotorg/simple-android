package org.simple.clinic.recentpatientsview

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Named

class LatestRecentPatientsUiRenderer @AssistedInject constructor(
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Assisted private val ui: LatestRecentPatientsUi,
    @Assisted private val numberOfPatientsToShow: Int
) : ViewRenderer<LatestRecentPatientsModel> {

  @AssistedInject.Factory
  interface Factory {
    fun create(ui: LatestRecentPatientsUi, numberOfPatientsToShow: Int): LatestRecentPatientsUiRenderer
  }

  override fun render(model: LatestRecentPatientsModel) {
    if (model.hasLoadedRecentPatients) {
      renderRecentPatients(model)
    }
  }

  private fun renderRecentPatients(model: LatestRecentPatientsModel) {
    val today = LocalDate.now(userClock)

    val recentPatientItems = addSeeAllIfListTooLong(
        recentPatients = model.recentPatients!!.map { recentPatientItem(it, today) },
        recentPatientLimit = numberOfPatientsToShow
    )

    ui.updateRecentPatients(recentPatientItems)
  }

  private fun recentPatientItem(recentPatient: RecentPatient, today: LocalDate): RecentPatientItem {
    val patientRegisteredOnDate = recentPatient.patientRecordedAt.toLocalDateAtZone(userClock.zone)
    val isNewRegistration = today == patientRegisteredOnDate

    return RecentPatientItem(
        uuid = recentPatient.uuid,
        name = recentPatient.fullName,
        age = age(recentPatient),
        gender = recentPatient.gender,
        updatedAt = recentPatient.updatedAt,
        dateFormatter = dateFormatter,
        clock = userClock,
        isNewRegistration = isNewRegistration
    )
  }

  private fun age(recentPatient: RecentPatient): Int {
    return DateOfBirth.fromRecentPatient(recentPatient, userClock).estimateAge(userClock)
  }

  private fun addSeeAllIfListTooLong(
      recentPatients: List<RecentPatientItem>,
      recentPatientLimit: Int
  ) = if (recentPatients.size > recentPatientLimit) {
    recentPatients.take(recentPatientLimit) + SeeAllItem
  } else {
    recentPatients
  }
}
