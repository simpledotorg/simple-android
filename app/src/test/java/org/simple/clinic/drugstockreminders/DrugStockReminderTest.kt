package org.simple.clinic.drugstockreminders

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.simple.clinic.FakeCall
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.NotFound
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.OtherError
import java.util.Optional
import java.util.UUID

class DrugStockReminderTest {

  private val drugStockReminderApi = mock<DrugStockReminderApi>()
  private val drugStockFormUrl = mock<Preference<Optional<String>>>()
  private val drugStockReminder = DrugStockReminder(
      drugStockReminderApi = drugStockReminderApi,
      drugStockFormUrl = drugStockFormUrl
  )

  @Test
  fun `when the network call fails, then other error must be returned`() {
    // given
    val fakeApiCall = FakeCall.failure<DrugStockReminderResponsePayload>(RuntimeException())
    val previousMonthsDate = "2022-02-01"
    whenever(drugStockReminderApi.drugStockReminder(previousMonthsDate)).thenReturn(fakeApiCall)

    // when
    val response = drugStockReminder.reminderForDrugStock(date = previousMonthsDate)

    // then
    assertThat(response).isEqualTo(OtherError)
  }

  @Test
  fun `when any response code apart from 200, 404 is received, the other error result must be returned`() {
    // given
    val fakeApiCall = FakeCall.error<DrugStockReminderResponsePayload>(
        data = "",
        responseCode = 500
    )
    val previousMonthsDate = "2022-02-01"
    whenever(drugStockReminderApi.drugStockReminder(previousMonthsDate)).thenReturn(fakeApiCall)

    // when
    val response = drugStockReminder.reminderForDrugStock(date = previousMonthsDate)

    // then
    assertThat(response).isEqualTo(OtherError)
  }

  @Test
  fun `when the server returns HTTP 404, then drug stock reports for previous month are not found`() {
    // given
    val previousMonthsDate = "2022-01-01"
    val fakeCall = FakeCall.error<DrugStockReminderResponsePayload>(
        data = "",
        responseCode = 404
    )
    whenever(drugStockReminderApi.drugStockReminder(previousMonthsDate)).thenReturn(fakeCall)

    // when
    val response = drugStockReminder.reminderForDrugStock(date = previousMonthsDate)

    // then
    assertThat(response).isEqualTo(NotFound)
  }

  @Test
  fun `when response code is 200 and drug stock reports are empty, then not found must be returned`() {
    // given
    val previousMonthsDate = "2022-01-01"
    val fakeCall = FakeCall.success(
        response = DrugStockReminderResponsePayload(
            month = previousMonthsDate,
            facilityUuid = UUID.fromString("76e4cbc5-91dd-4af5-ae0c-2b7948073858"),
            drugStockFormUrl = "drug_stock_form_url",
            drugs = emptyList()
        ),
        responseCode = 200
    )
    whenever(drugStockReminderApi.drugStockReminder(previousMonthsDate)).thenReturn(fakeCall)

    // when
    val response = drugStockReminder.reminderForDrugStock(date = previousMonthsDate)

    // then
    assertThat(response).isEqualTo(NotFound)
  }
}
