package org.simple.clinic.home.overdue.search

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
import org.simple.clinic.home.overdue.search.OverdueButtonType.DOWNLOAD
import org.simple.clinic.home.overdue.search.OverdueButtonType.SELECT_ALL
import org.simple.clinic.home.overdue.search.OverdueButtonType.SHARE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class OverdueSearchUpdateTest {

  private val date = LocalDate.of(2022, 2, 23)
  private val updateSpec = UpdateSpec(OverdueSearchUpdate(
      date = date,
      canGeneratePdf = true
  ))
  private val defaultModel = OverdueSearchModel.create()

  @Test
  fun `when overdue search results are loaded, then update model`() {
    val searchInputs = listOf("Babri")
    val facilityUuid = UUID.fromString("7dba16a0-1090-41f6-8e0c-0d97989de898")
    val overdueAppointments = listOf(TestData.overdueAppointment(
        facilityUuid = facilityUuid,
        patientUuid = UUID.fromString("37259e96-e757-4608-aeae-f1a20b088f09"),
        name = "Anish Acharya"
    ), TestData.overdueAppointment(
        facilityUuid = facilityUuid,
        patientUuid = UUID.fromString("53659148-a157-4aa4-92fb-c0a7991ae872"),
        name = "Anirban Dar"
    ))

    val overdueSearchResults = PagingData.from(overdueAppointments)
    val searchInputsChangedModel = defaultModel.overdueSearchInputsChanged(searchInputs)

    updateSpec
        .given(searchInputsChangedModel)
        .whenEvent(OverdueSearchResultsLoaded(overdueSearchResults))
        .then(
            assertThatNext(
                hasModel(
                    searchInputsChangedModel.overdueSearchResultsLoaded(overdueSearchResults)
                ),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when overdue patient is clicked, then open patient summary`() {
    val patientUuid = UUID.fromString("4831397d-4314-49fb-9ec7-3ae0a70c25c1")
    updateSpec
        .given(defaultModel)
        .whenEvent(OverduePatientClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patientUuid))
        ))
  }

  @Test
  fun `when call overdue patient is clicked, then open contact patient sheet`() {
    val patientUuid = UUID.fromString("4831397d-4314-49fb-9ec7-3ae0a70c25c1")
    updateSpec
        .given(defaultModel)
        .whenEvent(CallPatientClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenContactPatientSheet(patientUuid))
        ))
  }

  @Test
  fun `when load state is changed, then update the model`() {
    val loadStateChangedModel = defaultModel
        .loadStateChanged(DONE)

    updateSpec
        .given(loadStateChangedModel)
        .whenEvent(OverdueSearchLoadStateChanged(IN_PROGRESS))
        .then(assertThatNext(
            hasModel(loadStateChangedModel.loadStateChanged(IN_PROGRESS)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when selected overdue appointments ids are loaded, then update the model`() {
    val selectedAppointmentIds = setOf(UUID.fromString("dd1708db-9683-4699-b92f-152af3dda147"))

    updateSpec
        .given(defaultModel)
        .whenEvent(SelectedOverdueAppointmentsLoaded(selectedAppointmentIds))
        .then(assertThatNext(
            hasModel(defaultModel.selectedOverdueAppointmentsChanged(selectedAppointmentIds)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when overdue appointment checkbox is clicked, then toggle overdue appointment selection`() {
    val appointmentId = UUID.fromString("446a79bd-305b-444d-b400-974ea74f01ad")
    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueAppointmentCheckBoxClicked(appointmentId))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ToggleOverdueAppointmentSelection(appointmentId))
        ))
  }

  @Test
  fun `when clear all button is clicked, then clear selected overdue appointments`() {
    val selectedAppointmentsModel = defaultModel
        .selectedOverdueAppointmentsChanged(setOf(UUID.fromString("11502361-d887-401b-8b19-adeec7ed2db6")))

    updateSpec
        .given(selectedAppointmentsModel)
        .whenEvent(ClearSelectedOverdueAppointmentsClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ClearSelectedOverdueAppointments)
        ))
  }

  @Test
  fun `when download button is clicked, appointments are selected and pdf can be generated, then open select download format dialog`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val selectedAppointmentIdsModel = defaultModel.selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(DownloadButtonClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectDownloadFormatDialog)
        ))
  }

  @Test
  fun `when download button is clicked, appointments are selected and pdf cannot be generated, then schedule download`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val updateSpec = UpdateSpec(OverdueSearchUpdate(date = date, canGeneratePdf = false))
    val selectedAppointmentIdsModel = defaultModel.selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(DownloadButtonClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ScheduleDownload)
        ))
  }

  @Test
  fun `when download button is clicked and no appointments are selected, then load search results appointment ids`() {
    val searchInputsModel = defaultModel
        .overdueSearchInputsChanged(listOf("Ani"))

    updateSpec
        .given(searchInputsModel)
        .whenEvent(DownloadButtonClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasModel(searchInputsModel.loadStateChanged(IN_PROGRESS)),
            hasEffects(LoadSearchResultsAppointmentIds(buttonType = DOWNLOAD, searchInputs = listOf("Ani"), since = date))
        ))
  }

  @Test
  fun `when selected ids are replaced and pdf can be generated, then open select download format dialog`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val selectedAppointmentIdsModel = defaultModel
        .selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(SelectedAppointmentIdsReplaced(DOWNLOAD))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectDownloadFormatDialog)
        ))
  }

  @Test
  fun `when selected ids are replaced and pdf cannot be generated, then schedule download`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val updateSpec = UpdateSpec(OverdueSearchUpdate(date = date, canGeneratePdf = false))
    val selectedAppointmentIdsModel = defaultModel.selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(SelectedAppointmentIdsReplaced(DOWNLOAD))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ScheduleDownload)
        ))
  }

  @Test
  fun `when share button is clicked, appointments are selected and pdf can be generated, then open select share format dialog`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val selectedAppointmentIdsModel = defaultModel.selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(ShareButtonClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectShareFormatDialog)
        ))
  }

  @Test
  fun `when share button is clicked, appointments are selected and pdf cannot be generated, then open share in progress dialog`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val updateSpec = UpdateSpec(OverdueSearchUpdate(date = date, canGeneratePdf = false))
    val selectedAppointmentIdsModel = defaultModel.selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(ShareButtonClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenShareInProgressDialog)
        ))
  }

  @Test
  fun `when share button is clicked and no appointments are selected, then load search results appointment ids`() {
    val searchInputsModel = defaultModel
        .overdueSearchInputsChanged(listOf("Ani"))

    updateSpec
        .given(searchInputsModel)
        .whenEvent(ShareButtonClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasModel(searchInputsModel.loadStateChanged(IN_PROGRESS)),
            hasEffects(LoadSearchResultsAppointmentIds(buttonType = SHARE, searchInputs = listOf("Ani"), since = date))
        ))
  }

  @Test
  fun `when selected ids are replaced and pdf can be generated, then open select share format dialog`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val selectedAppointmentIdsModel = defaultModel
        .selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(SelectedAppointmentIdsReplaced(SHARE))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectShareFormatDialog)
        ))
  }

  @Test
  fun `when selected ids are replaced and pdf cannot be generated, then open share in progress dialog`() {
    val selectedAppointmentIds = setOf(UUID.fromString("ad32f91d-3c7e-45c3-b544-fbdc996e44b3"))
    val updateSpec = UpdateSpec(OverdueSearchUpdate(date = date, canGeneratePdf = false))
    val selectedAppointmentIdsModel = defaultModel.selectedOverdueAppointmentsChanged(selectedAppointmentIds)

    updateSpec
        .given(selectedAppointmentIdsModel)
        .whenEvent(SelectedAppointmentIdsReplaced(SHARE))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenShareInProgressDialog)
        ))
  }

  @Test
  fun `when download button is clicked and internet is not active, then show no internet connection dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadButtonClicked(networkStatus = Optional.of(INACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoInternetConnectionDialog)
        ))
  }

  @Test
  fun `when share button is clicked and internet is not active, then show no internet connection dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(ShareButtonClicked(networkStatus = Optional.of(INACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoInternetConnectionDialog)
        ))
  }

  @Test
  fun `when select all button is clicked and no appointments are selected, then load search results appointment ids`() {
    val searchInputsModel = defaultModel
        .overdueSearchInputsChanged(listOf("Ani"))

    updateSpec
        .given(searchInputsModel)
        .whenEvent(SelectAllButtonClicked)
        .then(assertThatNext(
            hasModel(searchInputsModel.loadStateChanged(IN_PROGRESS)),
            hasEffects(LoadSearchResultsAppointmentIds(buttonType = SELECT_ALL, searchInputs = listOf("Ani"), since = date))
        ))
  }

  @Test
  fun `when search results appointment ids are loaded and button type is download, then replace the selected appointment ids`() {
    val searchResultsAppointmentIds = setOf(UUID.fromString("cccff90c-f739-40af-8cf3-05b4e3e8297b"))

    updateSpec
        .given(defaultModel)
        .whenEvent(SearchResultsAppointmentIdsLoaded(
            buttonType = DOWNLOAD,
            searchResultsAppointmentIds = searchResultsAppointmentIds
        ))
        .then(assertThatNext(
            hasModel(defaultModel.loadStateChanged(DONE)),
            hasEffects(ReplaceSelectedAppointmentIds(appointmentIds = searchResultsAppointmentIds, type = DOWNLOAD))
        ))
  }

  @Test
  fun `when search results appointment ids are loaded and button type is share, then replace the selected appointment ids`() {
    val searchResultsAppointmentIds = setOf(UUID.fromString("91f96958-1247-498b-8742-1b05c92ecb3e"))

    updateSpec
        .given(defaultModel)
        .whenEvent(SearchResultsAppointmentIdsLoaded(
            buttonType = SHARE,
            searchResultsAppointmentIds = searchResultsAppointmentIds
        ))
        .then(assertThatNext(
            hasModel(defaultModel.loadStateChanged(DONE)),
            hasEffects(ReplaceSelectedAppointmentIds(appointmentIds = searchResultsAppointmentIds, type = SHARE))
        ))
  }

  @Test
  fun `when search results appointment ids are loaded and button type is select all, then select all search results appointment ids`() {
    val searchResultsAppointmentIds = setOf(UUID.fromString("91f96958-1247-498b-8742-1b05c92ecb3e"))

    updateSpec
        .given(defaultModel)
        .whenEvent(SearchResultsAppointmentIdsLoaded(
            buttonType = SELECT_ALL,
            searchResultsAppointmentIds = searchResultsAppointmentIds
        ))
        .then(assertThatNext(
            hasModel(defaultModel.loadStateChanged(DONE)),
            hasEffects(SelectAllAppointmentIds(appointmentIds = searchResultsAppointmentIds))
        ))
  }

  @Test
  fun `when villages and patient names are loaded, then update the model`() {
    val villagesAndPatientNames = listOf("Anand", "Anup", "Asia", "Earth")

    updateSpec
        .given(defaultModel)
        .whenEvent(VillagesAndPatientNamesLoaded(villagesAndPatientNames = villagesAndPatientNames))
        .then(
            assertThatNext(
                hasModel(defaultModel.villagesAndPatientNamesLoaded(villageAndPatientNames = villagesAndPatientNames)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when overdue search inputs are changed, then update model and search overdue patients`() {
    val searchInputs = listOf("Anand", "Anup", "Asia", "Earth")

    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueSearchInputsChanged(searchInputs))
        .then(assertThatNext(
            hasModel(defaultModel.overdueSearchInputsChanged(searchInputs)),
            hasEffects(SearchOverduePatients(
                searchInputs = searchInputs,
                since = date
            ))
        ))
  }
}
