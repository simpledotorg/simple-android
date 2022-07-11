package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.analytics.NetworkConnectivityStatus.ACTIVE
import org.simple.clinic.analytics.NetworkConnectivityStatus.INACTIVE
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.AGREED_TO_VISIT
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.MORE_THAN_A_YEAR_OVERDUE
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.PENDING_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMIND_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMOVED_FROM_OVERDUE
import org.simple.clinic.home.overdue.PendingListState.SEE_ALL
import org.simple.clinic.home.overdue.PendingListState.SEE_LESS
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import org.simple.sharedTestCode.TestData
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class OverdueUpdateTest {

  private val dateOnClock = LocalDate.parse("2018-01-01")
  private val updateSpec = UpdateSpec(OverdueUpdate(date = dateOnClock,
      canGeneratePdf = true,
      isOverdueSectionsFeatureEnabled = false))
  private val defaultModel = OverdueModel.create()

  @Test
  fun `when overdue patient is clicked, then open patient summary screen`() {
    val patientUuid = UUID.fromString("1211bce0-0b5d-4203-b5e3-004709059eca")

    updateSpec
        .given(defaultModel)
        .whenEvent(OverduePatientClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patientUuid))
        ))
  }

  @Test
  fun `when overdue appointments are loaded, then show overdue appointments`() {
    val overdueAppointments = PagingData.from(listOf(
        TestData.overdueAppointment_Old(appointmentUuid = UUID.fromString("4e4baeba-3a8e-4453-ace1-d3149088aefc")),
        TestData.overdueAppointment_Old(appointmentUuid = UUID.fromString("79c4bda9-50cf-4484-8a2a-c5336ce8af84"))
    ))
    val facility = TestData.facility(
        uuid = UUID.fromString("6d66fda7-7ca6-4431-ac3b-b570f1123624"),
        facilityConfig = FacilityConfig(
            diabetesManagementEnabled = true,
            teleconsultationEnabled = false
        )
    )
    val facilityLoadedModel = defaultModel
        .currentFacilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(OverdueAppointmentsLoaded_Old(overdueAppointments))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowOverdueAppointments(overdueAppointments, isDiabetesManagementEnabled = true))
        ))
  }

  @Test
  fun `when current facility is loaded and overdue sections feature is disabled, then load pending overdue appointments`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("6d66fda7-7ca6-4431-ac3b-b570f1123624"),
        facilityConfig = FacilityConfig(
            diabetesManagementEnabled = true,
            teleconsultationEnabled = false
        )
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.currentFacilityLoaded(facility)),
            hasEffects(LoadOverdueAppointments_old(dateOnClock, facility))
        ))
  }

  @Test
  fun `when current facility is loaded and overdue sections feature is enabled, then load overdue appointments`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("6d66fda7-7ca6-4431-ac3b-b570f1123624"),
        facilityConfig = FacilityConfig(
            diabetesManagementEnabled = true,
            teleconsultationEnabled = false
        )
    )

    val updateSpec = UpdateSpec(OverdueUpdate(
        date = dateOnClock,
        canGeneratePdf = true,
        isOverdueSectionsFeatureEnabled = true
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.currentFacilityLoaded(facility)),
            hasEffects(LoadOverdueAppointments(dateOnClock, facility))
        ))
  }

  @Test
  fun `when download overdue list button is clicked and network is not connected, then show no active connection dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadOverdueListClicked(networkStatus = Optional.of(INACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoActiveNetworkConnectionDialog)
        ))
  }

  @Test
  fun `when download overdue list button is clicked, network is connected and pdf can be generated, then open select download format dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadOverdueListClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectDownloadFormatDialog)
        ))
  }

  @Test
  fun `when download overdue list button is clicked, network is connected and pdf can not be generated, then schedule download`() {
    val updateSpec = UpdateSpec(OverdueUpdate(date = dateOnClock, canGeneratePdf = false, isOverdueSectionsFeatureEnabled = false))

    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadOverdueListClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ScheduleDownload(CSV))
        ))
  }

  @Test
  fun `when share overdue list button is clicked and network is not connected, then show no active connection dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(ShareOverdueListClicked(networkStatus = Optional.of(INACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoActiveNetworkConnectionDialog)
        ))
  }

  @Test
  fun `when share overdue list button is clicked, network is connected and pdf can be generated, then open select share format dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(ShareOverdueListClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectShareFormatDialog)
        ))
  }

  @Test
  fun `when share overdue list button is clicked, network is connected but pdf can not be generated, then open progress for share dialog`() {
    val updateSpec = UpdateSpec(OverdueUpdate(date = dateOnClock, canGeneratePdf = false, isOverdueSectionsFeatureEnabled = false))

    updateSpec
        .given(defaultModel)
        .whenEvent(ShareOverdueListClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSharingInProgressDialog)
        ))
  }

  @Test
  fun `when overdue appointments are loaded, then update the model`() {
    val pendingAppointments = listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("ad63a726-f0ab-4e95-a20e-bd394b4c7d3c"))
    )
    val agreedToVisitAppointments = listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("372871f0-0b11-4217-926f-9c5f2dce8202"))
    )
    val remindToCallLaterAppointments = listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("09ad7724-b3e2-4b1c-b490-7d7951b4150d"))
    )
    val removedFromOverdueAppointments = listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("e52d4555-d72d-4dfd-9b3e-21ead416e727"))
    )
    val moreThanAnYearOverdueAppointments = listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("20bb3b3a-908e-49b5-97ef-730eb2504bd9"))
    )

    val overdueAppointmentSections = OverdueAppointmentSections(
        pendingAppointments = pendingAppointments,
        agreedToVisitAppointments = agreedToVisitAppointments,
        remindToCallLaterAppointments = remindToCallLaterAppointments,
        removedFromOverdueAppointments = removedFromOverdueAppointments,
        moreThanAnYearOverdueAppointments = moreThanAnYearOverdueAppointments
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueAppointmentsLoaded(
            overdueAppointmentSections = overdueAppointmentSections
        ))
        .then(assertThatNext(
            hasModel(defaultModel.overdueAppointmentsLoaded(
                overdueAppointmentSections = overdueAppointmentSections
            )),
            hasNoEffects()
        ))
  }

  @Test
  fun `when pending list footer is clicked and pending list state is see less, then change the pending list state to see all`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PendingListFooterClicked)
        .then(
            assertThatNext(
                hasModel(defaultModel.pendingListStateChanged(pendingListState = SEE_ALL)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when pending list footer is clicked and pending list state is see all, then change the pending list state to see less`() {
    updateSpec
        .given(defaultModel.pendingListStateChanged(pendingListState = SEE_ALL))
        .whenEvent(PendingListFooterClicked)
        .then(
            assertThatNext(
                hasModel(defaultModel.pendingListStateChanged(pendingListState = SEE_LESS)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when chevron is clicked in pending overdue header, then update the state in the model`() {
    updateSpec
        .given(defaultModel.pendingChevronStateIsChanged(true))
        .whenEvent(ChevronClicked(PENDING_TO_CALL))
        .then(
            assertThatNext(
                hasModel(defaultModel.pendingChevronStateIsChanged(false)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when chevron is clicked in agreed to visit overdue header, then update the state in the model`() {
    updateSpec
        .given(defaultModel.agreedToVisitChevronStateIsChanged(true))
        .whenEvent(ChevronClicked(AGREED_TO_VISIT))
        .then(
            assertThatNext(
                hasModel(defaultModel.agreedToVisitChevronStateIsChanged(false)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when chevron is clicked in removed from overdue header, then update the state in the model`() {
    updateSpec
        .given(defaultModel.removedFromOverdueChevronStateIsChanged(true))
        .whenEvent(ChevronClicked(REMOVED_FROM_OVERDUE))
        .then(
            assertThatNext(
                hasModel(defaultModel.removedFromOverdueChevronStateIsChanged(false)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when chevron is clicked in remind to call overdue header, then update the state in the model`() {
    updateSpec
        .given(defaultModel.remindToCallChevronStateIsChanged(true))
        .whenEvent(ChevronClicked(REMIND_TO_CALL))
        .then(
            assertThatNext(
                hasModel(defaultModel.remindToCallChevronStateIsChanged(false)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when chevron is clicked in more than an year overdue header, then update the state in the model`() {
    updateSpec
        .given(defaultModel.moreThanAYearChevronStateIsChanged(true))
        .whenEvent(ChevronClicked(MORE_THAN_A_YEAR_OVERDUE))
        .then(
            assertThatNext(
                hasModel(defaultModel.moreThanAYearChevronStateIsChanged(false)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when overdue search button is clicked, then open overdue search`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueSearchButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenOverdueSearch)
        ))
  }

  @Test
  fun `when overdue appointment checkbox is clicked and appointment is already selected, then unselect the appointment`() {
    val appointmentId = UUID.fromString("7959606f-1b5f-4134-931b-e22765d1707b")
    val selectedOverdueAppointments = setOf(appointmentId)
    val selectedOverdueAppointmentsModel = defaultModel
        .selectedOverdueAppointmentsChanged(selectedOverdueAppointments)

    updateSpec
        .given(selectedOverdueAppointmentsModel)
        .whenEvent(OverdueAppointmentCheckBoxClicked(appointmentId))
        .then(assertThatNext(
            hasModel(
                selectedOverdueAppointmentsModel
                    .selectedOverdueAppointmentsChanged(emptySet())
            ),
            hasNoEffects()
        ))
  }

  @Test
  fun `when overdue appointment checkbox is clicked and appointment is not already selected, then select the appointment`() {
    val appointmentId1 = UUID.fromString("7959606f-1b5f-4134-931b-e22765d1707b")
    val appointmentId2 = UUID.fromString("27734c02-dadc-460c-a8a5-cb9f9eb407d7")
    val selectedOverdueAppointments = setOf(appointmentId1)
    val selectedOverdueAppointmentsModel = defaultModel
        .selectedOverdueAppointmentsChanged(selectedOverdueAppointments)

    updateSpec
        .given(selectedOverdueAppointmentsModel)
        .whenEvent(OverdueAppointmentCheckBoxClicked(appointmentId2))
        .then(assertThatNext(
            hasModel(
                selectedOverdueAppointmentsModel
                    .selectedOverdueAppointmentsChanged(setOf(appointmentId1, appointmentId2))
            ),
            hasNoEffects()
        ))
  }

  @Test
  fun `when clear all button is clicked, then clear selected overdue appointments`() {
    val selectedAppointmentsModel = defaultModel
        .selectedOverdueAppointmentsChanged(setOf(UUID.fromString("c4db5ddf-48a1-4bad-a3ff-e5bf861a12d3")))

    updateSpec
        .given(selectedAppointmentsModel)
        .whenEvent(ClearSelectedOverdueAppointments)
        .then(assertThatNext(
            hasModel(selectedAppointmentsModel.selectedOverdueAppointmentsChanged(emptySet())),
            hasNoEffects()
        ))
  }
}
