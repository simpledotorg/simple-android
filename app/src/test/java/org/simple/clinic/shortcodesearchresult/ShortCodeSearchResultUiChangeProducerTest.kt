package org.simple.clinic.shortcodesearchresult

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Test

class ShortCodeSearchResultUiChangeProducerTest {
  @Test
  fun `it shows the loading screen when fetching patients`() {
    // given
    val fetchingPatients = ShortCodeSearchResultState.fetchingPatients("1234567")
    val ui = mock<ShortCodeSearchResultUi>()

    val states = PublishSubject.create<ShortCodeSearchResultState>()
    states
        .compose(ShortCodeSearchResultUiChangeProducer(Schedulers.trampoline()))
        .subscribe { uiChange -> uiChange(ui) }

    // when
    states.onNext(fetchingPatients)

    // then
    verify(ui).showLoading()
    verifyNoMoreInteractions(ui)
  }
}
