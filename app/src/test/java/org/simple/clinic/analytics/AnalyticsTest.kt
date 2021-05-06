package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.simple.clinic.analytics.MockAnalyticsReporter.Event
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.platform.analytics.DatabaseOptimizationEvent
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import java.time.Duration
import java.time.Instant
import java.util.UUID

class AnalyticsTest {

  private val user = AnalyticsUser(
      id = UUID.fromString("8d8c86a1-1c32-4e1b-96ba-a85bfee7b45c"),
      name = "Anish Acharya"
  )

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }

  @Test
  fun `when reporting interaction events without any reporters, no error should be thrown`() {
    Analytics.reportUserInteraction("Test")
  }

  @Test
  fun `when reporting screen change events without any reporters, no error should be thrown`() {
    Analytics.reportScreenChange("Screen 1", "Screen 2")
  }

  @Test
  fun `when reporting input validation errors without any reporters, no error should be thrown`() {
    Analytics.reportInputValidationError("Error")
  }

  @Test
  fun `when setting the logged in user without any reporters, no error should be thrown`() {
    Analytics.setLoggedInUser(user)
  }

  @Test
  fun `when setting the newly registered user without any reporters, no error should be thrown`() {
    Analytics.setNewlyRegisteredUser(user)
  }

  @Test
  fun `when reporting a network call without any reporters, no error should be thrown`() {
    Analytics.reportNetworkCall(
        url = "test",
        method = "get",
        responseCode = 1,
        contentLength = 1,
        durationMillis = 1)
  }

  @Test
  fun `when reporting a network timeout without any reporters, no error should be thrown`() {
    Analytics.reportNetworkTimeout(
        url = "test",
        method = "get",
        metered = true,
        networkTransportType = Analytics.NetworkTransportType.WIFI,
        downstreamBandwidthKbps = 100,
        upstreamBandwidthKbps = 100)
  }

  @Test
  fun `when reporting a time taken event without any reporters, no error should be thrown`() {
    Analytics.reportTimeTaken(
        operationName = "test",
        timeTaken = Duration.ofMillis(500L))
  }

  @Test
  fun `when reporting a data cleared event without any reporters, no error should be thrown`() {
    Analytics.reportDataCleared(
        patientCount = 1,
        bloodPressureCount = 1,
        appointmentCount = 1,
        prescribedDrugCount = 1,
        medicalHistoryCount = 1,
        since = Instant.EPOCH
    )
  }

  @Test
  fun `when reporting permission event without any reporters, no error should be thrown`() {
    Analytics.reportPermissionResult(
        permission = "permission",
        result = GRANTED
    )
  }

  @Test
  fun `when reporting a SQL operation without any reporters, no error should be thrown`() {
    Analytics.reportSqlOperation(
        dao = "UserRoomDao_Impl",
        method = "count",
        timeTaken = Duration.ofMillis(200)
    )
  }

  @Test
  fun `when clearing the user without any reporters, no error should be thrown`() {
    Analytics.clearUser()
  }

  @Test
  fun `when setting the logged in user, the property must also be set on the reporters`() {
    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    Analytics.setLoggedInUser(user)
    assertThat(reporter.user).isEqualTo(user)
    assertThat(reporter.isANewRegistration).isFalse()
  }

  @Test
  fun `when setting the newly registered user, the property must also be set on the reporters`() {
    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    Analytics.setNewlyRegisteredUser(user)
    assertThat(reporter.user).isEqualTo(user)
    assertThat(reporter.isANewRegistration).isTrue()
  }

  @Test
  fun `when clearing the user, the user ID must be cleared from the reporters`() {
    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    Analytics.setLoggedInUser(user)
    assertThat(reporter.user).isEqualTo(user)
    Analytics.clearUser()
    assertThat(reporter.user).isNull()
  }

  @Test
  fun `when multiple reporters are present, the logged in user must be set on all`() {
    val reporter1 = MockAnalyticsReporter()
    val reporter2 = MockAnalyticsReporter()

    Analytics.addReporter(reporter1, reporter2)
    Analytics.setLoggedInUser(user)

    assertThat(reporter1.user).isEqualTo(user)
    assertThat(reporter2.user).isEqualTo(user)

    Analytics.clearUser()

    assertThat(reporter1.user).isNull()
    assertThat(reporter2.user).isNull()
  }

  @Test
  fun `when multiple reporters are present, the newly registered user must be set on all`() {
    val reporter1 = MockAnalyticsReporter()
    val reporter2 = MockAnalyticsReporter()

    Analytics.addReporter(reporter1, reporter2)
    Analytics.setNewlyRegisteredUser(user)

    assertThat(reporter1.user).isEqualTo(user)
    assertThat(reporter2.user).isEqualTo(user)

    Analytics.clearUser()

    assertThat(reporter1.user).isNull()
    assertThat(reporter2.user).isNull()
  }

  @Test
  fun `when multiple reporters are present, events should be received by all`() {
    val reporter1 = MockAnalyticsReporter()
    val reporter2 = MockAnalyticsReporter()

    Analytics.addReporter(reporter1, reporter2)

    Analytics.reportUserInteraction("Test 1")
    Analytics.reportUserInteraction("Test 2")
    Analytics.reportTimeTaken("Operation 1", Duration.ofMillis(500L))
    Analytics.reportUserInteraction("Test 3")
    Analytics.reportScreenChange("Screen 1", "Screen 2")
    Analytics.reportInputValidationError("Error 1")
    Analytics.reportInputValidationError("Error 2")
    Analytics.reportTimeTaken("Operation 2", Duration.ofMinutes(1L).plusMillis(750L))
    Analytics.reportNetworkCall("Test 1", "GET", 200, 500, 400)
    Analytics.reportNetworkTimeout(
        url = "Test 1",
        method = "GET",
        metered = true,
        networkTransportType = Analytics.NetworkTransportType.WIFI,
        downstreamBandwidthKbps = 100,
        upstreamBandwidthKbps = 50)
    Analytics.reportNetworkCall("Test 2", "POST", 400, 1000, 300)
    Analytics.reportNetworkTimeout(
        url = "Test 3",
        method = "POST",
        metered = false,
        networkTransportType = Analytics.NetworkTransportType.CELLULAR,
        downstreamBandwidthKbps = 50,
        upstreamBandwidthKbps = 100)
    Analytics.reportTimeTaken("Operation 1", Duration.ofHours(3L).plusMinutes(30L).plusMillis(1L))
    Analytics.reportDataCleared(
        patientCount = 1,
        bloodPressureCount = 2,
        appointmentCount = 3,
        prescribedDrugCount = 4,
        medicalHistoryCount = 5,
        since = Instant.parse("2018-12-03T10:15:30.00Z")
    )
    Analytics.reportPermissionResult("permission_1", GRANTED)
    Analytics.reportPermissionResult("permission_2", DENIED)
    Analytics.reportDatabaseOptimizationEvent(DatabaseOptimizationEvent(
        sizeBeforeOptimizationBytes = 100L,
        sizeAfterOptimizationBytes = 50L,
        type = DatabaseOptimizationEvent.OptimizationType.PurgeDeleted
    ))
    Analytics.reportSqlOperation(
        dao = "UserRoomDao_Impl",
        method = "count",
        timeTaken = Duration.ofSeconds(2)
    )

    val expected = listOf(
        Event("UserInteraction", mapOf("name" to "Test 1")),
        Event("UserInteraction", mapOf("name" to "Test 2")),
        Event("TimeTaken", mapOf(
            "operationName" to "Operation 1",
            "timeTakenInMillis" to 500L)
        ),
        Event("UserInteraction", mapOf("name" to "Test 3")),
        Event("ScreenChange", mapOf("outgoing" to "Screen 1", "incoming" to "Screen 2")),
        Event("InputValidationError", mapOf("name" to "Error 1")),
        Event("InputValidationError", mapOf("name" to "Error 2")),
        Event("TimeTaken", mapOf(
            "operationName" to "Operation 2",
            "timeTakenInMillis" to 60750L)),
        Event("NetworkCall", mapOf(
            "url" to "Test 1", "method" to "GET", "responseCode" to 200, "contentLength" to 500, "durationMs" to 400)
        ),
        Event("NetworkTimeout", mapOf(
            "url" to "Test 1",
            "method" to "GET",
            "metered" to true,
            "transport" to Analytics.NetworkTransportType.WIFI,
            "downstreamKbps" to 100,
            "upstreamKbps" to 50)
        ),
        Event("NetworkCall", mapOf(
            "url" to "Test 2", "method" to "POST", "responseCode" to 400, "contentLength" to 1000, "durationMs" to 300)
        ),
        Event("NetworkTimeout", mapOf(
            "url" to "Test 3",
            "method" to "POST",
            "metered" to false,
            "transport" to Analytics.NetworkTransportType.CELLULAR,
            "downstreamKbps" to 50,
            "upstreamKbps" to 100)
        ),
        Event("TimeTaken", mapOf(
            "operationName" to "Operation 1",
            "timeTakenInMillis" to 12600001L)
        ),
        Event("DataCleared", mapOf(
            "pendingPatientCount" to 1,
            "pendingBpCount" to 2,
            "pendingAppointmentCount" to 3,
            "pendingPrescribedDrugCount" to 4,
            "pendingMedicalHistoryCount" to 5,
            "since" to "2018-12-03T10:15:30Z"
        )),
        Event("PermissionResult", mapOf(
            "permission" to "permission_1",
            "result" to GRANTED
        )),
        Event("PermissionResult", mapOf(
            "permission" to "permission_2",
            "result" to DENIED
        )),
        Event("DatabaseOptimized", mapOf(
            "sizeBeforeOptimizationBytes" to 100L,
            "sizeAfterOptimizationBytes" to 50L,
            "type" to DatabaseOptimizationEvent.OptimizationType.PurgeDeleted.analyticsName
        )),
        Event("SqlOperation", mapOf(
            "dao" to "UserRoomDao_Impl",
            "method" to "count",
            "timeTakenInMillis" to 2000L
        ))
    )

    assertThat(reporter1.receivedEvents).isEqualTo(expected)
    assertThat(reporter2.receivedEvents).isEqualTo(expected)
  }
}
