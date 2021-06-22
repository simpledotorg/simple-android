package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class AllRecentPatientsUpdateTest {

  @Test
  fun `when recent patients are loaded, then show recent patients`() {
    val defaultModel = AllRecentPatientsModel.create()
    val recentPatients = PagingData.from(listOf(
        TestData.recentPatient(uuid = UUID.fromString("68bb2fba-512f-4419-bdda-9b661e90b805")),
        TestData.recentPatient(uuid = UUID.fromString("1fb172bc-b705-4ffb-a5bd-425811c430ab"))
    ))

    UpdateSpec(AllRecentPatientsUpdate())
        .given(defaultModel)
        .whenEvent(RecentPatientsLoaded(recentPatients))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowRecentPatients(recentPatients))
        ))
  }
}
