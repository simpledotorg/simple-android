package org.simple.clinic.security

import io.reactivex.Single
import org.mindrot.jbcrypt.BCrypt
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import javax.inject.Inject

class BCryptPasswordHasher @Inject constructor() : PasswordHasher {

  override fun compare(hashed: String, password: String): Single<ComparisonResult> {
    return Single.fromCallable {
      val checkpw = BCrypt.checkpw(password, hashed)
      when (checkpw) {
        true -> SAME
        false -> DIFFERENT
      }
    }
  }

  override fun hash(password: String): Single<String> {
    return Single.fromCallable {
      BCrypt.hashpw(password, BCrypt.gensalt())
    }
  }
}
