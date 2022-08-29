package org.simple.clinic.plumbing.infrastructure

import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User

interface Infrastructure {

  fun addDetails(
      user: User,
      country: Country,
      deployment: Deployment
  )
}
