package org.simple.clinic.selectcountry.adapter

import android.view.View
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_selectcountry_country_view.*
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import org.simple.clinic.selectcountry.adapter.Event.CountryClicked
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX

data class SelectableCountryItem(
    val country: Country,
    val isSelected: Boolean,
    val showDivider: Boolean
) : ItemAdapter.Item<Event> {

  override fun layoutResId() = R.layout.list_selectcountry_country_view

  override fun render(holder: ViewHolderX, subject: Subject<Event>) {
    holder.countryButton.apply {
      // TODO(vs): 2019-11-06 Read the country name from string resources
      text = country.displayName
      isChecked = isSelected
      holder.divider.visibility = if (showDivider) View.VISIBLE else View.INVISIBLE
      setOnClickListener { subject.onNext(CountryClicked(country)) }
    }
  }

}
