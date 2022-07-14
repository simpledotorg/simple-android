package org.simple.clinic.overdue

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class OverdueAppointmentSelectorTest {

  private val overdueAppointmentSelector = OverdueAppointmentSelector()

  @Test
  fun `when appointment is selected, then update the selected appointment ids`() {
    // given
    val appointmentId = UUID.fromString("840d0c91-741f-422a-b305-1f950923983e")

    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst()).isEmpty()

    // when
    overdueAppointmentSelector.toggleSelection(appointmentId)

    // then
    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(appointmentId))
  }

  @Test
  fun `when appointment is unselected, then update the selected appointment ids`() {
    // given
    val appointmentId1 = UUID.fromString("d3147cc2-ea87-4fbd-be99-0c57d6907c95")
    val appointmentId2 = UUID.fromString("ef73af02-b2d8-40e7-b73f-e3095397f4b5")

    overdueAppointmentSelector.toggleSelection(appointmentId1)
    overdueAppointmentSelector.toggleSelection(appointmentId2)

    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(appointmentId1, appointmentId2))

    // when
    overdueAppointmentSelector.toggleSelection(appointmentId1)

    // then
    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(appointmentId2))
  }

  @Test
  fun `clearing selected appointment ids should work correctly`() {
    // given
    val appointmentId1 = UUID.fromString("8f7ee668-6cf1-4c1f-865a-b6d8dd5f6e07")
    val appointmentId2 = UUID.fromString("248cc8f1-43df-4f44-9134-bcbb568b69ee")

    overdueAppointmentSelector.toggleSelection(appointmentId1)
    overdueAppointmentSelector.toggleSelection(appointmentId2)

    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(appointmentId1, appointmentId2))

    // when
    overdueAppointmentSelector.clearSelection()

    // then
    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEmpty()
  }

  @Test
  fun `replace selected appointment ids should work correctly`() {
    // given
    val appointmentId1 = UUID.fromString("805d8630-3117-483c-8da9-590b9ab4f33c")
    val appointmentId2 = UUID.fromString("a035e60a-d2c3-49cb-a42a-68f577135c25")

    val newAppointmentId1 = UUID.fromString("0f8382b5-faa8-4bd3-ab5e-ce7f00a757b4")
    val newAppointmentId2 = UUID.fromString("f43fafa4-fa56-4fee-94aa-e0155cb13a30")

    overdueAppointmentSelector.toggleSelection(appointmentId1)
    overdueAppointmentSelector.toggleSelection(appointmentId2)

    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(appointmentId1, appointmentId2))

    // when
    overdueAppointmentSelector.replaceSelectedIds(setOf(newAppointmentId1, newAppointmentId2))

    // then
    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(newAppointmentId1, newAppointmentId2))
  }

  @Test
  fun `adding selected appointment ids should work correctly`() {
    // given
    val appointmentId1 = UUID.fromString("805d8630-3117-483c-8da9-590b9ab4f33c")
    val appointmentId2 = UUID.fromString("a035e60a-d2c3-49cb-a42a-68f577135c25")

    val newAppointmentId1 = UUID.fromString("0f8382b5-faa8-4bd3-ab5e-ce7f00a757b4")
    val newAppointmentId2 = UUID.fromString("f43fafa4-fa56-4fee-94aa-e0155cb13a30")

    overdueAppointmentSelector.toggleSelection(appointmentId1)
    overdueAppointmentSelector.toggleSelection(appointmentId2)

    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(appointmentId1, appointmentId2))

    // when
    overdueAppointmentSelector.addSelectedIds(setOf(newAppointmentId1, newAppointmentId2))

    // then
    assertThat(overdueAppointmentSelector.selectedAppointmentIdsStream.blockingFirst())
        .isEqualTo(setOf(appointmentId1, appointmentId2, newAppointmentId1, newAppointmentId2))
  }
}
