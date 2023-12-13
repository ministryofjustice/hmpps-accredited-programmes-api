package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi

import jakarta.annotation.PreDestroy
import org.springframework.boot.env.OriginTrackedMapPropertySource
import org.springframework.boot.origin.OriginTrackedValue
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.WiremockPortHolder

class TestPropertiesInitializer : ApplicationContextInitializer<ConfigurableApplicationContext?> {

  override fun initialize(applicationContext: ConfigurableApplicationContext?) {
    val wiremockPort = WiremockPortHolder.getPort()

    val upstreamServiceUrlsToOverride = mutableMapOf<String, String>()

    applicationContext!!.environment.propertySources
      .filterIsInstance<OriginTrackedMapPropertySource>()
      .filter { it.name.contains("application-test.yml") }
      .forEach { propertyFile ->
        propertyFile.source.forEach { (propertyName, propertyValue) ->
          if (propertyName.startsWith("services.") && (propertyValue as? OriginTrackedValue)?.value is String) {
            upstreamServiceUrlsToOverride[propertyName] = ((propertyValue as OriginTrackedValue).value as String).replace("#WIREMOCK_PORT", wiremockPort.toString())
            return@forEach
          }

          if (propertyName == "hmpps.auth.url" && (propertyValue as? OriginTrackedValue)?.value is String) {
            upstreamServiceUrlsToOverride[propertyName] = ((propertyValue as OriginTrackedValue).value as String).replace("#WIREMOCK_PORT", wiremockPort.toString())
          }
        }
      }

    TestPropertyValues
      .of(
        mapOf(
          "wiremock.port" to wiremockPort.toString(),
        ) + upstreamServiceUrlsToOverride,
      ).applyTo(applicationContext)
  }
}

@Component
class TestPropertiesDestructor {
  @PreDestroy
  fun destroy() = WiremockPortHolder.releasePort()
}
