package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type
import javax.inject.Inject

class OverdueSearchHistoryTest {

  @Inject
  lateinit var overdueSearchHistory: OverdueSearchHistory

  @Inject
  @TypedPreference(Type.OverdueSearchHistory)
  lateinit var overdueSearchHistoryPreference: Preference<String>

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    overdueSearchHistoryPreference.delete()
  }

  @Test
  fun fetching_overdue_search_history_should_work_correctly() {
    // given
    overdueSearchHistoryPreference.set("Narwar, Tandi")

    // when
    val searchHistory = overdueSearchHistory.fetch().blockingFirst()

    // then
    assertThat(searchHistory).isEqualTo(setOf(
        "Narwar",
        "Tandi"
    ))
  }

  @Test
  fun adding_search_query_to_search_history_should_work_as_expected() {
    // given
    val searchQuery = "Bathinda"
    val existingSearchHistory = "Babri, Ramesh, Mehta, Tandi, Narwar"

    overdueSearchHistoryPreference.set(existingSearchHistory)

    // when
    overdueSearchHistory.add(searchQuery)

    // then
    val searchHistory = overdueSearchHistory.fetch().blockingFirst()
    assertThat(searchHistory).isEqualTo(setOf(
        "Bathinda",
        "Babri",
        "Ramesh",
        "Mehta",
        "Tandi"
    ))
  }

  @Test
  fun adding_already_existing_search_query_to_history_should_move_it_to_top() {
    // given
    val searchQuery = "Mehta"
    val existingSearchHistory = "Babri, Ramesh, Mehta, Tandi, Narwar"

    overdueSearchHistoryPreference.set(existingSearchHistory)

    // when
    overdueSearchHistory.add(searchQuery)

    // then
    val searchHistory = overdueSearchHistory.fetch().blockingFirst()
    assertThat(searchHistory).isEqualTo(setOf(
        "Mehta",
        "Babri",
        "Ramesh",
        "Tandi",
        "Narwar"
    ))
  }
}
