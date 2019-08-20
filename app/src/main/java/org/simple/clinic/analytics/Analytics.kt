package org.simple.clinic.analytics

import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_LOWPAN
import android.net.NetworkCapabilities.TRANSPORT_VPN
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkCapabilities.TRANSPORT_WIFI_AWARE
import org.simple.clinic.user.User
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.UUID

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

  fun setLoggedInUser(user: User) {
    reporters.forEach {
      it.safely("Error setting logged in user!") {
        setLoggedInUser(user)
      }
    }
  }

  fun clearUser() {
    reporters.forEach {
      it.safely("Error clearing user ID!") {
        resetUser()
      }
    }
  }

  fun reportUserInteraction(name: String) {
    reporters.forEach {
      it.safely("Error reporting interaction!") {
        createEvent("UserInteraction", mapOf("name" to name))
      }
    }
  }

  fun reportScreenChange(outgoingScreen: String, incomingScreen: String) {
    reporters.forEach {
      it.safely("Error reporting screen change!") {
        createEvent("ScreenChange", mapOf("outgoing" to outgoingScreen, "incoming" to incomingScreen))
      }
    }
  }

  fun reportInputValidationError(error: String) {
    reporters.forEach {
      it.safely("Error reporting input validation!") {
        createEvent("InputValidationError", mapOf("name" to error))
      }
    }
  }

  fun reportViewedPatient(patientUuid: UUID, from: String) {
    reporters.forEach {
      it.safely("Error reporting viewed patient event!") {
        createEvent("ViewedPatient", mapOf("patientId" to patientUuid.toString(), "from" to from))
      }
    }
  }

  fun reportNetworkCall(url: String, method: String, responseCode: Int, contentLength: Int, durationMillis: Int) {
    reporters.forEach {
      it.safely("Error reporting network call") {
        createEvent("NetworkCall", mapOf(
            "url" to url,
            "method" to method,
            "responseCode" to responseCode,
            "contentLength" to contentLength,
            "durationMs" to durationMillis
        ))
      }
    }
  }

  fun reportNetworkTimeout(
      url: String,
      method: String,
      metered: Boolean,
      networkTransportType: NetworkTransportType,
      downstreamBandwidthKbps: Int,
      upstreamBandwidthKbps: Int
  ) {
    reporters.forEach {
      it.safely("Error reporting network timeout") {
        createEvent("NetworkTimeout", mapOf(
            "url" to url,
            "method" to method,
            "metered" to metered,
            "transport" to networkTransportType,
            "downstreamKbps" to downstreamBandwidthKbps,
            "upstreamKbps" to upstreamBandwidthKbps
        ))
      }
    }
  }

  fun reportTimeTaken(operationName: String, timeTaken: Duration) {
    reporters.forEach {
      it.safely("Error reporting time taken event") {
        createEvent("TimeTaken", mapOf(
            "operationName" to operationName,
            "timeTakenInMillis" to timeTaken.toMillis()
        ))
      }
    }
  }

  fun reportDataCleared(
      patientCount: Int,
      bloodPressureCount: Int,
      appointmentCount: Int,
      prescribedDrugCount: Int,
      medicalHistoryCount: Int,
      since: Instant
  ) {
    reporters.forEach {
      it.safely("Error reporting data cleared event") {
        createEvent("DataCleared", mapOf(
            "pendingPatientCount" to patientCount,
            "pendingBpCount" to bloodPressureCount,
            "pendingAppointmentCount" to appointmentCount,
            "pendingPrescribedDrugCount" to prescribedDrugCount,
            "pendingMedicalHistoryCount" to medicalHistoryCount,
            "since" to since.toString()
        ))
      }
    }
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
