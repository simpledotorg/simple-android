package org.simple.clinic.drugstockreminders

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.sharedTestCode.util.Rules
import javax.inject.Inject

class DrugStockReminderApiIntegrationTest {

  @Inject
  lateinit var drugStockReminder: DrugStockReminder

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun drug_stock_reports_are_filled_or_not_in_the_previous_month_should_be_fetched_to_show_reminders() {
    // when
    val result : Found = drugStockReminder.reminderForDrugStock(date = "2022-07-01") as Found

    // then
    assertThat(result).isNotNull()
    val drugStockReport = result.drugStockReminderResponse
    assertThat(drugStockReport.month).isNotEmpty()
    assertThat(drugStockReport.drugs).isNotNull()
    assertThat(drugStockReport.facilityUuid).isNotNull()
  }
}
