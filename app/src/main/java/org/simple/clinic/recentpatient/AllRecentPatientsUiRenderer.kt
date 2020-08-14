package org.simple.clinic.recentpatient

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.UserClock
import java.time.format.DateTimeFormatter
import javax.inject.Named

class AllRecentPatientsUiRenderer @AssistedInject constructor(
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Assisted private val ui: AllRecentPatientsUi
) : ViewRenderer<AllRecentPatientsModel> {

  @AssistedInject.Factory
  interface Factory {
    fun create(ui: AllRecentPatientsUi): AllRecentPatientsUiRenderer
  }

  override fun render(model: AllRecentPatientsModel) {
    if (model.hasLoadedRecentPatients) {
      renderRecentPatients(model)
    }
  }

  private fun renderRecentPatients(model: AllRecentPatientsModel) {
    val recentPatientItems = RecentPatientItem.create(model.recentPatients!!, userClock, dateFormatter)

    ui.updateRecentPatients(recentPatientItems)
  }
}
