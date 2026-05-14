package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.monitoring
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import java.lang.Exception

/**
 * Adds version data to the /health endpoint. This is called by the UI to display API details
 */
@Component
class HealthInfo(buildProperties: BuildProperties) : HealthIndicator {
  private val version: String = buildProperties.version ?: throw Exception("No version exists")

  override fun health(): Health = Health.up().withDetail("version", version).build()
}
