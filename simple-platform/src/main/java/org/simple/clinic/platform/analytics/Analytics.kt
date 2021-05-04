package org.simple.clinic.platform.analytics

import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_LOWPAN
import android.net.NetworkCapabilities.TRANSPORT_VPN
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkCapabilities.TRANSPORT_WIFI_AWARE
import org.simple.clinic.platform.util.RuntimePermissionResult
import java.time.Duration
import java.time.Instant

object Analytics {
  private var reporters: List<AnalyticsReporter> = emptyList()

  fun addReporter(vararg reportersToAdd: AnalyticsReporter) {
    reporters = reporters + reportersToAdd
  }

  fun clearReporters() {
    reporters = emptyList()
  }

  fun removeReporter(reporter: AnalyticsReporter) {
    reporters = reporters - reporter
  }

  fun setLoggedInUser(user: AnalyticsUser) {
    reporters.forEach { it.setLoggedInUser(user, false) }
  }

  fun setNewlyRegisteredUser(user: AnalyticsUser) {
    reporters.forEach { it.setLoggedInUser(user, true) }
  }

  fun clearUser() {
    reporters.forEach(AnalyticsReporter::resetUser)
  }

  fun reportUserInteraction(name: String) {
    val props = mapOf("name" to name)

    reporters.forEach { it.createEvent("UserInteraction", props) }
  }

  fun reportScreenChange(outgoingScreen: String, incomingScreen: String) {
    val props = mapOf(
        "outgoing" to outgoingScreen,
        "incoming" to incomingScreen
    )

    reporters.forEach { it.createEvent("ScreenChange", props) }
  }

  fun reportInputValidationError(error: String) {
    val props = mapOf("name" to error)

    reporters.forEach { it.createEvent("InputValidationError", props) }
  }

  fun reportNetworkCall(url: String, method: String, responseCode: Int, contentLength: Int, durationMillis: Int) {
    val props = mapOf(
        "url" to url,
        "method" to method,
        "responseCode" to responseCode,
        "contentLength" to contentLength,
        "durationMs" to durationMillis
    )

    reporters.forEach { it.createEvent("NetworkCall", props) }
  }

  fun reportNetworkTimeout(
      url: String,
      method: String,
      metered: Boolean,
      networkTransportType: NetworkTransportType,
      downstreamBandwidthKbps: Int,
      upstreamBandwidthKbps: Int
  ) {
    val props = mapOf(
        "url" to url,
        "method" to method,
        "metered" to metered,
        "transport" to networkTransportType,
        "downstreamKbps" to downstreamBandwidthKbps,
        "upstreamKbps" to upstreamBandwidthKbps
    )

    reporters.forEach { it.createEvent("NetworkTimeout", props) }
  }

  fun reportTimeTaken(operationName: String, timeTaken: Duration) {
    val props = mapOf(
        "operationName" to operationName,
        "timeTakenInMillis" to timeTaken.toMillis()
    )

    reporters.forEach { it.createEvent("TimeTaken", props) }
  }

  fun reportDataCleared(
      patientCount: Int,
      bloodPressureCount: Int,
      appointmentCount: Int,
      prescribedDrugCount: Int,
      medicalHistoryCount: Int,
      since: Instant
  ) {
    val props = mapOf(
        "pendingPatientCount" to patientCount,
        "pendingBpCount" to bloodPressureCount,
        "pendingAppointmentCount" to appointmentCount,
        "pendingPrescribedDrugCount" to prescribedDrugCount,
        "pendingMedicalHistoryCount" to medicalHistoryCount,
        "since" to since.toString()
    )

    reporters.forEach { it.createEvent("DataCleared", props) }
  }

  fun reportPermissionResult(
      permission: String,
      result: RuntimePermissionResult
  ) {
    val props = mapOf(
        "permission" to permission,
        "result" to result
    )
    reporters.forEach { it.createEvent("PermissionResult", props) }
  }

  fun reportDatabaseOptimizationEvent(
      event: DatabaseOptimizationEvent
  ) {
    val props = mapOf(
        "sizeBeforeOptimizationBytes" to event.sizeBeforeOptimizationBytes,
        "sizeAfterOptimizationBytes" to event.sizeAfterOptimizationBytes,
        "type" to event.type.analyticsName
    )

    reporters.forEach { it.createEvent("DatabaseOptimized", props) }
  }

  enum class NetworkTransportType {
    BLUETOOTH,
    CELLULAR,
    ETHERNET,
    LOWPAN,
    VPN,
    WIFI,
    WIFI_AWARE,
    OTHER;

    companion object {
      fun fromNetworkCapabilities(capabilities: NetworkCapabilities): NetworkTransportType {
        return when {
          capabilities.hasTransport(TRANSPORT_WIFI) -> WIFI
          capabilities.hasTransport(TRANSPORT_CELLULAR) -> CELLULAR
          capabilities.hasTransport(TRANSPORT_ETHERNET) -> ETHERNET
          capabilities.hasTransport(TRANSPORT_VPN) -> VPN
          capabilities.hasTransport(TRANSPORT_LOWPAN) -> LOWPAN
          capabilities.hasTransport(TRANSPORT_WIFI_AWARE) -> WIFI_AWARE
          capabilities.hasTransport(TRANSPORT_BLUETOOTH) -> BLUETOOTH
          else -> OTHER
        }
      }
    }
  }
}
