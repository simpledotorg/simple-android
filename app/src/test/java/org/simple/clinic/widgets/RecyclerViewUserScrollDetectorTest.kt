package org.simple.clinic.widgets

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxbinding3.recyclerview.RecyclerViewScrollEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.RxErrorsRule

@RunWith(JUnitParamsRunner::class)
class RecyclerViewUserScrollDetectorTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  @Test
  @Parameters(value = [
    "10, ${RecyclerView.SCROLL_STATE_IDLE}, false",
    "10, ${RecyclerView.SCROLL_STATE_DRAGGING}, true",
    "10, ${RecyclerView.SCROLL_STATE_SETTLING}, false",

    "0, ${RecyclerView.SCROLL_STATE_IDLE}, false",
    "0, ${RecyclerView.SCROLL_STATE_DRAGGING}, false",
    "0, ${RecyclerView.SCROLL_STATE_SETTLING}, false",

    "-10, ${RecyclerView.SCROLL_STATE_IDLE}, false",
    "-10, ${RecyclerView.SCROLL_STATE_DRAGGING}, true",
    "-10, ${RecyclerView.SCROLL_STATE_SETTLING}, false"
  ])
  fun `scrolls by the user should be detected correctly`(
      dy: Int,
      scrollState: Int,
      expectedToBeByUser: Boolean
  ) {
    val scrollEvent = mock<RecyclerViewScrollEvent>()
    whenever(scrollEvent.dy).thenReturn(dy)

    val scrollEvents = Observable.just(scrollEvent)
    val scrollStateChanges = Observable.just(scrollState)

    val detected = Observables.combineLatest(scrollEvents, scrollStateChanges)
        .compose(RecyclerViewUserScrollDetector.streamDetections())
        .blockingFirst()

    assertThat(detected).isEqualTo(RecyclerViewScrolled(byUser = expectedToBeByUser))
  }
}
