package org.simple.clinic.allpatientsinfacility_old

import androidx.recyclerview.widget.DiffUtil
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityListItem.SectionTitle.None
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityListItem.SectionTitle.Text
import org.simple.clinic.widgets.ItemAdapter
import java.util.Locale

class AllPatientsInFacilityListAdapter(
    diffCallback: DiffUtil.ItemCallback<AllPatientsInFacilityListItem>,
    private val locale: Locale
) : ItemAdapter<AllPatientsInFacilityListItem, AllPatientsInFacilityListItem.Event>(diffCallback), FastScrollRecyclerView.SectionedAdapter {

  override fun getSectionName(position: Int): String {
    return when (val sectionTitle = getItem(position).sectionTitle(locale)) {
      is None -> ""
      is Text -> sectionTitle.title
    }
  }
}
