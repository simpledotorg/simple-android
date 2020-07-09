package org.simple.clinic.recentpatientsview

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.patient.PatientConfig

class LatestRecentPatientsInit(
    private val numberOfPatientsToShow: Int
) : Init<LatestRecentPatientsModel, LatestRecentPatientsEffect> {

  companion object {
    fun create(config: PatientConfig): LatestRecentPatientsInit = LatestRecentPatientsInit(
        numberOfPatientsToShow = config.recentPatientLimit
    )
  }

  override fun init(model: LatestRecentPatientsModel): First<LatestRecentPatientsModel, LatestRecentPatientsEffect> {
    // Fetching an extra recent patient to know whether we have more than "recentPatientLimit" number of recent patients
    // TODO (vs) 09/07/20: Move this hack to a separate flow
    val loadRecentPatients = LoadRecentPatients(count = numberOfPatientsToShow + 1)

    return first(model, loadRecentPatients)
  }
}
