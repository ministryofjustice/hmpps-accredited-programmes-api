package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.mock.http.MockHttpOutputMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer.toDomain
import java.nio.charset.StandardCharsets

class CsvHttpMessageConverterTest {

  private val converter = CsvHttpMessageConverter()

  @Nested
  inner class ReaderTests {
    @Test
    fun `can read`() {
      converter
        .canRead(
          listOf(MyBean("A", "B", "C"))::class.java,
          MediaType("text", "csv", Charsets.UTF_8),
        )
        .shouldBeTrue()
    }

    @Test
    fun `read csv to a List of MyBean`() {
      val body = "name,description,comment\nA,Desc A,It's an A\nB,Desc B,It's a B\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MediaType("text", "csv")
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<MyBean>>()
      list shouldContainExactly (
        listOf(
          MyBean(name = "A", description = "Desc A", comment = "It's an A"),
          MyBean(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Tolerant conversion - accept csv with extra columns`() {
      val body = "name,description,comment,,,\nA,Desc A,It's an A,,,\nB,Desc B,It's a B,,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MediaType("text", "csv")
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<MyBean>>()
      list shouldContainExactly (
        listOf(
          MyBean(name = "A", description = "Desc A", comment = "It's an A"),
          MyBean(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Tolerant conversion - accept csv with varying extra columns`() {
      val body = "name,description,comment\nA,Desc A,It's an A,\nB,Desc B,It's a B,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))

      inputMessage.headers.contentType = MediaType("text", "csv")
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<MyBean>>()
      list shouldContainExactly (
        listOf(
          MyBean(name = "A", description = "Desc A", comment = "It's an A"),
          MyBean(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Tolerant conversion - accept csv with re-ordered columns`() {
      val body = ",comment,name,description\n,It's an A,A,Desc A,,\n,It's a B,B,Desc B,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MediaType("text", "csv")
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<MyBean>>()
      list shouldContainExactly (
        listOf(
          MyBean(name = "A", description = "Desc A", comment = "It's an A"),
          MyBean(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Tolerant conversion - accept csv with unknown property name`() {
      val body = "unknown1,comment,unknown2,name,description\n,It's an A,,A,Desc A,,\n,It's a B,,B,Desc B,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<MyBean>>()
      list shouldContainExactly (
        listOf(
          MyBean(name = "A", description = "Desc A", comment = "It's an A"),
          MyBean(name = "B", description = "Desc B", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `Tolerant conversion - accept csv with empty columns`() {
      val body = "name,description,comment,,,\n,Desc A,It's an A,,,\nB,,It's a B,,,\n"

      val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.UTF_8))
      inputMessage.headers.contentType = MediaType("text", "csv")
      val result = converter.read(listOfBeanType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<MyBean>>()
      list shouldContainExactly (
        listOf(
          MyBean(name = "", description = "Desc A", comment = "It's an A"),
          MyBean(name = "B", description = "", comment = "It's a B"),
        )
        )
    }

    @Test
    fun `read courses csv to a List of CourseRecord`() {
      val inputMessage = MockHttpInputMessage(CsvTestData.coursesCsvInputStream())
      inputMessage.headers.contentType = MediaType("text", "csv")
      val result = converter.read(listOfCourseRecordType, null, inputMessage)
      val list = result.shouldBeInstanceOf<List<CourseRecord>>()
      list.map(CourseRecord::toDomain).shouldContainExactly(CsvTestData.newCourses)
    }
  }

  @Nested
  inner class WriterTests {
    @Test
    fun `can write empty list`() {
      val outputMessage = MockHttpOutputMessage()
      converter.write(emptyList<MyBean>(), listOfBeanType, MediaType("text", "csv"), outputMessage)
      outputMessage.body.toString() shouldBe ""
    }

    @Test
    fun `can write a list of two beans`() {
      val outputMessage = MockHttpOutputMessage()
      val beans = listOf(
        MyBean(name = "", description = "Desc A", comment = "It's an A"),
        MyBean(name = "B", description = "Desc B"),
      )

      converter.write(beans, listOfBeanType, MediaType("text", "csv"), outputMessage)

      outputMessage.body.toString() shouldBe
        """
        comment,description,name
        "It's an A","Desc A",
        ,"Desc B",B
        
        """.trimIndent()
    }

    @Test
    fun `A CourseRecords round trip should be the identity transformation`() {
      val outputMessage = MockHttpOutputMessage()

      converter.write(
        CsvTestData.newCourseUpdates.toCourseRecords(),
        listOfCourseRecordType,
        MediaType("text", "csv"),
        outputMessage,
      )

      val inputMessage = MockHttpInputMessage(outputMessage.bodyAsBytes)
      inputMessage.headers.contentType = MediaType("text", "csv")

      val result = converter.read(listOfCourseRecordType, null, inputMessage)
      val roundTrippedCourseRecords = result.shouldBeInstanceOf<List<CourseRecord>>()
      roundTrippedCourseRecords.map(CourseRecord::toDomain).shouldContainExactly(CsvTestData.newCourseUpdates)
    }
  }
}

private fun List<CourseUpdate>.toCourseRecords() = map {
  CourseRecord(
    name = it.name,
    description = it.description,
    audience = it.audience,
    identifier = it.identifier,
    referable = it.referable,
    alternateName = it.alternateName,
  )
}

private val listOfBeanType = object : ParameterizedTypeReference<List<MyBean>>() {}.type
private val listOfCourseRecordType = object : ParameterizedTypeReference<List<CourseRecord>>() {}.type

data class MyBean(
  val name: String? = null,
  val description: String? = null,
  val comment: String? = null,
)
