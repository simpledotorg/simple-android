package org.simple.clinic.navigation.v2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class History(val requests: List<NavRequest>) : Parcelable {

  init {
    if (requests.isEmpty()) throw IllegalStateException("Require at least 1 key!")
  }

  companion object {
    fun ofNormalScreens(vararg screens: ScreenKey): History {
      return History(screens.map(::Normal))
    }
  }

  fun add(request: NavRequest): History {
    return copy(requests = requests + request)
  }

  fun removeLast(): History {
    require(requests.size > 1) {
      val lastKey = requests.lastOrNull()?.key
      "Cannot remove last key ($lastKey) when there is only one key left"
    }

    return copy(requests = requests.dropLast(1))
  }

  fun removeUntil(key: ScreenKey): History {
    return removeWhile { it != key }
  }

  fun removeUntilInclusive(key: ScreenKey): History {
    return removeWhile { it != key }.removeLast()
  }

  inline fun removeWhile(predicate: (ScreenKey) -> Boolean): History {
    val newRequests = requests.dropLastWhile { navRequest -> predicate(navRequest.key) }

    return copy(requests = newRequests)
  }

  fun top(): NavRequest {
    return requests.last()
  }

  fun isNavRequestBeforeAnother(first: NavRequest, second: NavRequest?): Boolean {
    return requests.indexOf(first) < requests.indexOf(second)
  }

  fun matchesTop(navRequest: NavRequest): Boolean {
    return requests.lastOrNull() == navRequest
  }
}
