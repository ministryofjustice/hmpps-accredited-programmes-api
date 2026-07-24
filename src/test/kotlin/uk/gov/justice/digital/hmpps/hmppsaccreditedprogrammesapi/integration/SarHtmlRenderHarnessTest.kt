package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.subjectaccessrequest.rendering.RenderRequestInfo
import uk.gov.justice.digital.hmpps.subjectaccessrequest.templates.RenderParameters
import uk.gov.justice.digital.hmpps.subjectaccessrequest.templates.TemplateDataFetcherFacade
import uk.gov.justice.digital.hmpps.subjectaccessrequest.templates.TemplateHelpers
import uk.gov.justice.digital.hmpps.subjectaccessrequest.templates.TemplateRenderService
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

/**
 * Dev-only harness: renders a captured SAR JSON payload to HTML using the same
 * template + helpers the aggregator uses in production. Skipped unless
 * `APG_SAR_HTML_INPUT` points at a JSON file. See README for usage.
 */
class SarHtmlRenderHarnessTest {

  @Test
  fun `render SAR JSON to HTML`() {
    val inputPath = System.getenv("APG_SAR_HTML_INPUT")
    assumeTrue(!inputPath.isNullOrBlank(), "APG_SAR_HTML_INPUT not set")

    val jsonPath: Path = Paths.get(inputPath)
    check(Files.exists(jsonPath)) { "input file not found: $jsonPath" }

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    // The API's SAR response is { "content": { ... } }; the template renders against content.
    val rootNode = objectMapper.readTree(jsonPath.toFile())
    val contentNode = if (rootNode.has("content")) rootNode.get("content") else rootNode
    val data = objectMapper.treeToValue(contentNode, Map::class.java)

    val template = this::class.java.classLoader
      .getResource("sar_template.mustache")!!
      .readText()

    val helpers = TemplateHelpers(mock<TemplateDataFetcherFacade>(), objectMapper)
    val renderer = TemplateRenderService(helpers)

    val rendered = renderer.renderServiceTemplate(
      RenderParameters(templateVersion = "1.0", template = template, data = data),
      RenderRequestInfo(UUID.randomUUID(), "sar-html-renderer"),
    ).toString(StandardCharsets.UTF_8)

    val outDir = System.getenv("APG_SAR_HTML_OUT") ?: System.getProperty("java.io.tmpdir")
    val baseName = jsonPath.fileName.toString().removeSuffix(".json")
    val outPath = Paths.get(outDir, "apg-sar-rendered-$baseName.html")
    Files.writeString(outPath, rendered, StandardCharsets.UTF_8)

    println("Rendered ${rendered.length} chars -> ${outPath.toUri()}")
  }
}
