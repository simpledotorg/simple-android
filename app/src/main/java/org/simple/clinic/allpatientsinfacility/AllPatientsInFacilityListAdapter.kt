package org.simple.clinic.allpatientsinfacility

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.SectionTitle.None
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.SectionTitle.Text
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import java.util.Locale

class AllPatientsInFacilityListAdapter(
    diffCallback: DiffUtil.ItemCallback<AllPatientsInFacilityListItem>,
    private val locale: Locale
) : ListAdapter<AllPatientsInFacilityListItem, ViewHolderX>(diffCallback), FastScrollRecyclerView.SectionedAdapter {

  private val eventSubject = PublishSubject.create<AllPatientsInFacilityListItem.Event>()

  val listItemEvents: Observable<AllPatientsInFacilityListItem.Event> = eventSubject.hide()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderX {
    return ViewHolderX(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolderX, position: Int) {
    getItem(position).render(holder, eventSubject)
  }

  override fun getItemViewType(position: Int): Int {
    return getItem(position).layoutResId
  }

  override fun getSectionName(position: Int): String {
    return when (val sectionTitle = getItem(position).sectionTitle(locale)) {
      is None -> ""
      is Text -> sectionTitle.title
    }
  }
}
