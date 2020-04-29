package org.simple.clinic.security

import org.mindrot.jbcrypt.BCrypt
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import javax.inject.Inject

class BCryptPasswordHasher @Inject constructor() : PasswordHasher {

  override fun compare(hashed: String, password: String): ComparisonResult {
    return if (BCrypt.checkpw(password, hashed)) SAME else DIFFERENT
  }

  override fun hash(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
  }
}
