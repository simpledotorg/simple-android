package org.simple.clinic.allpatientsinfacility

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.SectionTitle.None
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.SectionTitle.Text
import org.simple.clinic.widgets.BindingItemAdapter
import java.util.Locale

class AllPatientsInFacilityListAdapter(
    diffCallback: DiffUtil.ItemCallback<AllPatientsInFacilityListItem>,
    bindings: Map<Int, (layoutInflater: LayoutInflater, parent: ViewGroup) -> ViewBinding>,
    private val locale: Locale
) : BindingItemAdapter<AllPatientsInFacilityListItem, AllPatientsInFacilityListItem.Event>(diffCallback, bindings), FastScrollRecyclerView.SectionedAdapter {

  override fun getSectionName(position: Int): String {
    return when (val sectionTitle = getItem(position).sectionTitle(locale)) {
      is None -> ""
      is Text -> sectionTitle.title
    }
  }
}
