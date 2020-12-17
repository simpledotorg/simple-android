package org.simple.clinic.selectcountry.adapter

import android.view.View
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.displayname.CountryDisplayNameFetcher
import org.simple.clinic.databinding.ListSelectcountryCountryViewBinding
import org.simple.clinic.selectcountry.adapter.Event.CountryClicked
import org.simple.clinic.widgets.BindingItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

data class SelectableCountryItem(
    val country: Country,
    private val isCountryChosenByUser: Boolean,
    private val showDivider: Boolean,
    private val countryDisplayNameFetcher: CountryDisplayNameFetcher
) : BindingItemAdapter.Item<Event> {

  companion object {
    fun from(countries: List<Country>, chosenCountry: Country?, countryDisplayNameFetcher: CountryDisplayNameFetcher): List<SelectableCountryItem> {
      return countries
          .mapIndexed { index, country ->
            val isLastCountryInList = index == countries.lastIndex
            val hasUserChosenCountry = country == chosenCountry

            SelectableCountryItem(
                country = country,
                isCountryChosenByUser = hasUserChosenCountry,
                showDivider = !isLastCountryInList,
                countryDisplayNameFetcher = countryDisplayNameFetcher
            )
          }
    }
  }

  override fun layoutResId() = R.layout.list_selectcountry_country_view

  override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    val binding = holder.binding as ListSelectcountryCountryViewBinding

    binding.countryButton.apply {
      text = countryDisplayNameFetcher.displayNameForCountry(country)
      isChecked = isCountryChosenByUser
      binding.divider.visibility = if (showDivider) View.VISIBLE else View.INVISIBLE
      setOnClickListener { subject.onNext(CountryClicked(country)) }
    }
  }
}
