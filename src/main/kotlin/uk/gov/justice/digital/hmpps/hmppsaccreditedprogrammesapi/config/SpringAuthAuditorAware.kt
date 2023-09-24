package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Optional

@Component
@Primary
class SpringAuthAuditorAware : AuditorAware<String> {
  override fun getCurrentAuditor() = Optional.ofNullable(
    when (val principal = SecurityContextHolder.getContext().authentication?.principal) {
      is String -> principal
      is UserDetails -> principal.username
      is Map<*, *> -> principal["username"] as String
      else -> null
    },
  )
}
