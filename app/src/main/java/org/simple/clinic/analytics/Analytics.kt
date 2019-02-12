package org.simple.clinic.analytics

import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.*
import java.util.UUID

object Analytics {
  private var reporters: List<AnalyticsReporter> = emptyList()

  fun addReporter(vararg reportersToAdd: AnalyticsReporter) {
    reporters += reportersToAdd
  }

  fun clearReporters() {
    reporters = emptyList()
  }

  fun removeReporter(reporter: AnalyticsReporter) {
    reporters -= reporter
  }

  fun setUserId(uuid: UUID) {
    reporters.forEach {
      it.safely("Error setting user ID!") {
        val uuidString = uuid.toString()

        setUserIdentity(uuidString)
        setProperty("userId", uuidString)
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
