package org.simple.clinic.drugstockreminders

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.util.TestUtcClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@Ignore("remove ignore annotation after server implements the drug stock reminder api")
class DrugStockReminderApiIntegrationTest {

  @Inject
  lateinit var drugStockReminder: DrugStockReminder

  @Inject
  lateinit var clock: TestUtcClock

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.parse("2022-03-01"))
  }

  @Test
  fun drug_stock_reports_are_filled_or_not_in_the_previous_month_should_be_fetched_to_show_reminders() {
    // given
    val dateFormat = DateTimeFormatter.ofPattern("YYYY-MM-DD", Locale.ENGLISH)
    val previousMonthsDate = LocalDateTime.now(clock).minusMonths(1)

    val formattedDate = dateFormat.format(previousMonthsDate)

    // when
    val result = drugStockReminder.reminderForDrugStock(formattedDate) as Found

    // then
    assertThat(result.drugStockReports).hasSize(1)
    val drugStockReport = result.drugStockReports.first()
    assertThat(drugStockReport.drugsInStock).isEqualTo(20)
    assertThat(drugStockReport.drugsReceived).isEqualTo(20)
    assertThat(drugStockReport.protocolDrugId).isEqualTo("2a6b78c3-a5ca-41d3-8ec9-70c0d7aaea79")
  }
}
