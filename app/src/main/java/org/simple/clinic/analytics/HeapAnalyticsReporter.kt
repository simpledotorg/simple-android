package org.simple.clinic.analytics

import android.content.Context
import com.heapanalytics.android.Heap
import org.simple.clinic.BuildConfig

class HeapAnalyticsReporter(context: Context, debug: Boolean = false) : AnalyticsReporter {

  init {
    Heap.init(context.applicationContext, BuildConfig.HEAP_ID, debug)
  }

  override fun setUserIdentity(id: String) {
    Heap.identify(id)
  }

  override fun resetUserIdentity() {
    Heap.resetIdentity()
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    Heap.track(event, props.mapValues { (_, value) -> value.toString() })
  }
}
