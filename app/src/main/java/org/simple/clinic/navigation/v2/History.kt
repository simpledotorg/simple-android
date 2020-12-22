
package org.simple.clinic.navigation.v2

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class History(val requests: List<NavRequest>) : Parcelable {

  init {
    if (requests.isEmpty()) throw IllegalStateException("Require at least 1 key!")
  }

  fun add(request: NavRequest): History {
    return copy(requests = requests + request)
  }

  fun removeLast(): History {
    require(requests.size > 1) { "Cannot remove last when there is only one key left" }

    return copy(requests = requests.dropLast(1))
  }

  fun removeUntil(key: ScreenKey): History {
    val newRequests = requests.dropLastWhile { it.key != key }

    return copy(requests = newRequests)
  }

  fun top(): NavRequest {
    return requests.last()
  }
}
