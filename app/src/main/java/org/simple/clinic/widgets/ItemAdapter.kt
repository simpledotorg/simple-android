package org.simple.clinic.widgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.widgets.recyclerview.ViewHolderX

open class ItemAdapter<I : ItemAdapter.Item<E>, E>(
    diffCallback: DiffUtil.ItemCallback<I>
) : ListAdapter<I, ViewHolderX>(diffCallback) {

  private val eventSubject: Subject<E> = PublishSubject.create<E>()

  val itemEvents: Observable<E> = eventSubject.hide()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderX {
    return ViewHolderX(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolderX, position: Int) {
    getItem(position).render(holder, eventSubject)
  }

  override fun getItemViewType(position: Int): Int {
    return getItem(position).layoutResId()
  }

  interface Item<E> {

    @LayoutRes
    fun layoutResId(): Int

    fun render(holder: ViewHolderX, subject: Subject<E>)
  }
}
