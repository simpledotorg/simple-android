package org.simple.clinic.sync

interface ModelSync {
  val name: String
  val requiresSyncApprovedUser: Boolean

  fun push()
  fun pull()
}
