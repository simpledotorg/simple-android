package org.simple.clinic.allpatientsinfacility

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX

class AllPatientsInFacilityListAdapter : ListAdapter<AllPatientsInFacilityListItem, ViewHolderX>(
    AllPatientsInFacilityListItem.AllPatientsInFacilityListItemCallback()
) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderX {
    return ViewHolderX(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolderX, position: Int) {
    getItem(position).render(holder)
  }

  override fun getItemViewType(position: Int): Int {
    return getItem(position).layoutResId
  }
}
