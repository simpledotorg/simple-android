package org.simple.clinic.recentpatientsview

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.UserClock
import java.time.format.DateTimeFormatter
import javax.inject.Named

class LatestRecentPatientsUiRenderer @AssistedInject constructor(
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Assisted private val ui: LatestRecentPatientsUi,
    @Assisted private val numberOfPatientsToShow: Int
) : ViewRenderer<LatestRecentPatientsModel> {

  @AssistedFactory
  interface Factory {
    fun create(ui: LatestRecentPatientsUi, numberOfPatientsToShow: Int): LatestRecentPatientsUiRenderer
  }

  override fun render(model: LatestRecentPatientsModel) {
    if (model.hasLoadedRecentPatients) {
      renderRecentPatients(model)
      renderEmptyView(model)
    }
  }

  private fun renderRecentPatients(model: LatestRecentPatientsModel) {
    val recentPatientItems = addSeeAllIfListTooLong(
        recentPatients = RecentPatientItemType.create(model.recentPatients!!, userClock, dateFormatter),
        recentPatientLimit = numberOfPatientsToShow
    )

    ui.updateRecentPatients(recentPatientItems)
  }

  private fun renderEmptyView(model: LatestRecentPatientsModel) {
    if (model.isAtLeastOneRecentPatientPresent) {
      ui.showOrHideRecentPatients(true)
    } else {
      ui.showOrHideRecentPatients(false)
    }
  }

  private fun addSeeAllIfListTooLong(
      recentPatients: List<RecentPatientItemType>,
      recentPatientLimit: Int
  ) = if (recentPatients.size > recentPatientLimit) {
    recentPatients.take(recentPatientLimit) + SeeAllItem
  } else {
    recentPatients
  }
}
