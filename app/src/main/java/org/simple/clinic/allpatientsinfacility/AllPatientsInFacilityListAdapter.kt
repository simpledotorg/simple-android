package org.simple.clinic.allpatientsinfacility

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.widgets.recyclerview.ViewHolderX

class AllPatientsInFacilityListAdapter(
    diffCallback: DiffUtil.ItemCallback<AllPatientsInFacilityListItem>
) : ListAdapter<AllPatientsInFacilityListItem, ViewHolderX>(diffCallback) {

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
}
