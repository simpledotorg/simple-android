package org.simple.clinic.plumbing.infrastructure

import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User

class FakeInfrastructure : Infrastructure {

  var user: User? = null
  var country: Country? = null
  var deployment: Deployment? = null

  override fun addDetails(user: User, country: Country, deployment: Deployment) {
    this.user = user
    this.country = country
    this.deployment = deployment
  }

  override fun clear() {
    this.user = null
    this.country = null
    this.deployment = null
  }
}
