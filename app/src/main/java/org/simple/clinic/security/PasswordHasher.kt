package org.simple.clinic.security

import io.reactivex.Single

interface PasswordHasher {

  fun hash(password: String): Single<String>

  fun compare(hashed: String, password: String): Single<ComparisonResult>
}
