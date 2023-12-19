package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SecurityService {
  fun getCurrentUserName() = SecurityContextHolder.getContext().authentication?.name
}
