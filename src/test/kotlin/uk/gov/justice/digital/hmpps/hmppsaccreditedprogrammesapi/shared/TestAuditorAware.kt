package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shared

import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Component
import java.util.Optional

const val AUDITOR_AWARE_TEST_USER_NAME = "Test User Name"

@Component
@EnableJpaAuditing(modifyOnCreate = false)
class TestAuditorAware : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.of(AUDITOR_AWARE_TEST_USER_NAME)
}
