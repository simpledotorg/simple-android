package org.simple.clinic.home

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.simple.clinic.R
import org.simple.clinic.home.overdue.OverdueScreen
import org.simple.clinic.home.patients.PatientsScreen
import org.simple.clinic.home.report.ReportsScreen

class HomePagerAdapter(private val context: Context) : PagerAdapter() {

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val inflater = LayoutInflater.from(container.context)

    val screenForTab = inflater.inflate(HomeTabs.values()[position].key, container, false)
    container.addView(screenForTab)

    return screenForTab
  }

  override fun destroyItem(container: ViewGroup, position: Int, viewKey: Any) {
    container.removeView(viewKey as View)
  }

  override fun getPageTitle(position: Int): CharSequence = context.resources.getString(HomeTabs.values()[position].title)

  override fun getCount() = HomeTabs.values().size

  override fun isViewFromObject(view: View, viewKey: Any) = view === viewKey
}

private enum class HomeTabs(@LayoutRes val key: Int, @StringRes val title: Int) {

  PATIENT(PatientsScreen.KEY.layoutRes(), R.string.tab_patient),

  OVERDUE(OverdueScreen.KEY.layoutRes(), R.string.tab_overdue),

  REPORTS(ReportsScreen.KEY.layoutRes(), R.string.tab_reports)
}
