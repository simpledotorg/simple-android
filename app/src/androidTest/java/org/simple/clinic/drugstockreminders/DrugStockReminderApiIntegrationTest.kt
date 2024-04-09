package org.simple.clinic.drugstockreminders

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.sharedTestCode.util.Rules
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

//@Ignore("remove ignore annotation after server implements the drug stock reminder api")
class DrugStockReminderApiIntegrationTest {

  @Inject
  lateinit var drugStockReminder: DrugStockReminder

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

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
    val previousMonthsDate = LocalDate.now(clock).minusMonths(1).toString()

    // when
    val result = drugStockReminder.reminderForDrugStock(previousMonthsDate) as Found

    // then
    assertThat(result).isNotNull()
    val drugStockReport = result.drugStockReminderResponse
    assertThat(drugStockReport.month).isNotEmpty()
    assertThat(drugStockReport.drugs).isNotNull()
    assertThat(drugStockReport.facilityUuid).isNotNull()
  }
}
