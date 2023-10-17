package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.config

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.mock.http.MockHttpOutputMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.MEDIA_TYPE_TEXT_CSV
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.generateCourseRecords
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.toCourseCsv
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config.CsvHttpMessageConverter
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class CsvHttpMessageConverterTest {

  private val converter = CsvHttpMessageConverter()

  @Nested
  inner class ReaderTests {
    @Test
    fun `Valid CSV should always be possible to convert to its domain equivalent`() {
      converter
        .canRead(
          listOf(PlaceholderRecord("A", "B", "C"))::class.java,
          MEDIA_TYPE_TEXT_CSV,
        )
        .shouldBeTrue()
    }

    @Test
    fun `Reading valid CSV should converts to the domain equivalent`() {
      val body = "name,description,comment\nA,Desc A,It's an A\nB,Desc B,It's a B\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MEDIA_TYPE_TEXT_CSV
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<PlaceholderRecord>>()
      list shouldContainExactly (
        listOf(
          PlaceholderRecord(name = "A", description = "Desc A", comment = "It's an A"),
          PlaceholderRecord(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Reading valid CSV with extra columns should tolerantly convert to the domain equivalent`() {
      val body = "name,description,comment,,,\nA,Desc A,It's an A,,,\nB,Desc B,It's a B,,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MEDIA_TYPE_TEXT_CSV
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<PlaceholderRecord>>()
      list shouldContainExactly (
        listOf(
          PlaceholderRecord(name = "A", description = "Desc A", comment = "It's an A"),
          PlaceholderRecord(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Reading valid CSV with varying columns should tolerantly convert to the domain equivalent`() {
      val body = "name,description,comment\nA,Desc A,It's an A,\nB,Desc B,It's a B,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))

      inputMessage.headers.contentType = MEDIA_TYPE_TEXT_CSV
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<PlaceholderRecord>>()
      list shouldContainExactly (
        listOf(
          PlaceholderRecord(name = "A", description = "Desc A", comment = "It's an A"),
          PlaceholderRecord(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Reading valid CSV and disordered columns should tolerantly convert to the domain equivalent`() {
      val body = ",comment,name,description\n,It's an A,A,Desc A,,\n,It's a B,B,Desc B,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MEDIA_TYPE_TEXT_CSV
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<PlaceholderRecord>>()
      list shouldContainExactly (
        listOf(
          PlaceholderRecord(name = "A", description = "Desc A", comment = "It's an A"),
          PlaceholderRecord(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Reading valid CSV with unknown property names should tolerantly convert to the domain equivalent`() {
      val body = "unknown1,comment,unknown2,name,description\n,It's an A,,A,Desc A,,\n,It's a B,,B,Desc B,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<PlaceholderRecord>>()
      list shouldContainExactly (
        listOf(
          PlaceholderRecord(name = "A", description = "Desc A", comment = "It's an A"),
          PlaceholderRecord(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Reading valid CSV with empty columns should tolerantly convert to the domain equivalent`() {
      val body = "name,description,comment,,,\n,Desc A,It's an A,,,\nB,,It's a B,,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MEDIA_TYPE_TEXT_CSV
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<PlaceholderRecord>>()
      list shouldContainExactly (
        listOf(
          PlaceholderRecord(name = "", description = "Desc A", comment = "It's an A"),
          PlaceholderRecord(name = "B", description = "", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Reading valid course records as CSV should convert them to the domain equivalent`() {
      val courseRecords = generateCourseRecords(3)
      val inputMessage = MockHttpInputMessage(ByteArrayInputStream(courseRecords.toCourseCsv().toByteArray()))
      inputMessage.headers.contentType = MEDIA_TYPE_TEXT_CSV
      val result = converter.read(listOfCourseRecordType, null, inputMessage)
      val convertedRecords = result.shouldBeInstanceOf<List<CourseRecord>>()
      convertedRecords.map { it.toDomain() }.shouldContainAll(courseRecords.map { it.toDomain() })
    }
  }

  @Nested
  inner class WriterTests {
    @Test
    fun `Writing an empty list should result in an empty string`() {
      val outputMessage = MockHttpOutputMessage()

      converter.write(
        emptyList<PlaceholderRecord>(),
        listOfBeanType,
        MEDIA_TYPE_TEXT_CSV,
        outputMessage,
      )

      val result = outputMessage.body.toString()
      result shouldBe ""
    }

    @Test
    fun `Writing a populated list of records should produce the correct CSV output`() {
      val outputMessage = MockHttpOutputMessage()
      val placeholderRecords = listOf(
        PlaceholderRecord(name = "", description = "Desc A", comment = "It's an A"),
        PlaceholderRecord(name = "B", description = "Desc B"),
      )

      converter.write(
        placeholderRecords,
        listOfBeanType,
        MEDIA_TYPE_TEXT_CSV,
        outputMessage,
      )

      val expectedOutput = "comment,description,name\n\"It's an A\",\"Desc A\",\n,\"Desc B\",B\n"
      val result = outputMessage.body.toString()
      result shouldBe expectedOutput
    }

    @Test
    fun `Converting course records to a CSV and back to a list of course records should maintain internal consistency`() {
      val initialCourseRecords = generateCourseRecords(3)

      val outputMessage = MockHttpOutputMessage()
      converter.write(initialCourseRecords, listOfCourseRecordType, MEDIA_TYPE_TEXT_CSV, outputMessage)

      val inputMessage = MockHttpInputMessage(outputMessage.bodyAsBytes)
      inputMessage.headers.contentType = MEDIA_TYPE_TEXT_CSV
      val readResult = converter.read(listOfCourseRecordType, null, inputMessage)

      val convertedCourseRecords = readResult.shouldBeInstanceOf<List<CourseRecord>>()
      convertedCourseRecords.map { it.toDomain() }.shouldContainExactly(initialCourseRecords.map { it.toDomain() })
    }
  }
}

private val listOfBeanType = object : ParameterizedTypeReference<List<PlaceholderRecord>>() {}.type

private val listOfCourseRecordType = object : ParameterizedTypeReference<List<CourseRecord>>() {}.type

data class PlaceholderRecord(
  val name: String? = null,
  val description: String? = null,
  val comment: String? = null,
)
