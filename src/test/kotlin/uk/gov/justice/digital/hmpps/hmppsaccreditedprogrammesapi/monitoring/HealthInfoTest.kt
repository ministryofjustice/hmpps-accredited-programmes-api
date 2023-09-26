package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.monitoring

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.info.BuildProperties
import java.util.Properties

class HealthInfoTest {
  @Test
  fun `Health info when queried should include version information from build properties`() {
    val properties = Properties()
    properties.setProperty("version", "somever")
    assertThat(HealthInfo(BuildProperties(properties)).health().details).isEqualTo(mapOf("version" to "somever"))
  }
}