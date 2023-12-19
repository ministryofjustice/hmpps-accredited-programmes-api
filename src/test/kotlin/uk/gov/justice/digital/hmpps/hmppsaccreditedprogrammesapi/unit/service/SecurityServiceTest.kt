package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CLIENT_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SecurityService

class SecurityServiceTest {

  private var securityService: SecurityService = SecurityService()

  @Test
  fun `Get username from token successful`() {
    mockSecurityContext(CLIENT_USERNAME)

    val currentUserName = securityService.getCurrentUserName()

    assertEquals(CLIENT_USERNAME, currentUserName)
  }

  private fun mockSecurityContext(username: String) {
    val authentication = mockk<Authentication>()
    every { authentication.name } returns username

    val securityContext = mockk<SecurityContext>()
    every { securityContext.authentication } returns authentication

    mockkStatic(SecurityContextHolder::class)
    every { SecurityContextHolder.getContext() } returns securityContext
  }
}
