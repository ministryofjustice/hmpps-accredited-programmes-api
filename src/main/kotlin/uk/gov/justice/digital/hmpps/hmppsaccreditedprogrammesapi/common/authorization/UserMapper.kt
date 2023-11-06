package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.authorization

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class UserMapper {
  fun getUsername() =
    when (val principal = SecurityContextHolder.getContext().authentication?.principal) {
      is String -> principal
      is UserDetails -> principal.username
      is Map<*, *> -> principal["username"] as String
      else -> null
    }
}
