package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import java.util.UUID

class ConfirmRemoveBloodSugarUpdateTest {

  private val bloodSugarMeasurementUuid = UUID.fromString("1d4eaf96-1ded-4aca-999e-1648b81add86")
  private val defaultModel = ConfirmRemoveBloodSugarModel.create(bloodSugarMeasurementUuid)
  private val updateSpec = UpdateSpec<ConfirmRemoveBloodSugarModel, ConfirmRemoveBloodSugarEvent, ConfirmRemoveBloodSugarEffect>(ConfirmRemoveBloodSugarUpdate())

  @Test
  fun `when remove is clicked in the confirm remove blood sugar dialog, then remove blood sugar`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RemoveBloodSugarClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkBloodSugarAsDeleted(bloodSugarMeasurementUuid) as ConfirmRemoveBloodSugarEffect)
        ))
  }

  @Test
  fun `when blood sugar is deleted, then close the confirm remove blood sugar dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarMarkedAsDeleted)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseConfirmRemoveBloodSugarDialog as ConfirmRemoveBloodSugarEffect)
        ))
  }

}
