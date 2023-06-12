package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.mock.http.MockHttpInputMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursesPutRequestInner
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

class CsvHttpMessageConverterTest {

  private val converter = CsvHttpMessageConverter()

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
    val beanList = object : ParameterizedTypeReference<List<MyBean>>() {}
    val result = converter.read(beanList.type, null, inputMessage)
    val list = result.shouldBeInstanceOf<List<MyBean>>()
    list shouldContainExactly (
      listOf(
        MyBean(name = "A", description = "Desc A", comment = "It's an A"),
        MyBean(name = "B", description = "Desc B", comment = "It's a B"),
      )
      )
  }

  @Test
  fun `read csv to a List of CoursePutRequestInner`() {
    val inputMessage = MockHttpInputMessage(fromResource("Courses.csv"))
    inputMessage.headers.contentType = MediaType("text", "csv")
    val beanList = object : ParameterizedTypeReference<List<CoursesPutRequestInner>>() {}
    val result = converter.read(beanList.type, null, inputMessage)
    val list = result.shouldBeInstanceOf<List<CoursesPutRequestInner>>()
    list.shouldContainExactly(
      listOf(
        CoursesPutRequestInner(name="Becoming New Me Plus", description="Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", audience="Sexual offence, Intimate partner violence, Non-intimate partner violence", acronym="BNM+", comments="General comment: Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " ),
        CoursesPutRequestInner(name="Building Better Relationships", description="Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ", audience="Intimate partner violence ", acronym="BBR", comments="" ),
        CoursesPutRequestInner(name="Healthy Identity Intervention", description="Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Extremism offence", acronym="HI", comments=""),
        CoursesPutRequestInner(name="Healthy Sex Programme", description="Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Sexual offence", acronym="HSP", comments=""),
        CoursesPutRequestInner(name="Horizon", description="Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Sexual offence", acronym="", comments="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."),
        CoursesPutRequestInner(name="iHorizon", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ", audience="Sexual offence", acronym="", comments="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."),
        CoursesPutRequestInner(name="Identity Matters", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ", audience="Gang offence, Extremism offence", acronym="IM" , comments=""),
        CoursesPutRequestInner(name="Kaizen", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Violent offence", acronym="", comments=""),
        CoursesPutRequestInner(name="Kaizen", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Intimate partner violence", acronym="", comments=""),
        CoursesPutRequestInner(name="Kaizen", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Sexual offence", acronym="", comments=""),
        CoursesPutRequestInner(name="Living as New Me (custody)", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Violent offence, Sexual offence, Intimate partner violence ", acronym="LNM", comments=""),
        CoursesPutRequestInner(name="Living as New Me (community)", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Sexual offence", acronym="LNM", comments=""),
        CoursesPutRequestInner(name="Motivation and Engagement", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Violent offence, Sexual offence, Intimate partner violence ", acronym="M&E", comments=""),
        CoursesPutRequestInner(name="New Me MOT", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ", audience="Violent offence, Sexual offence, Intimate partner violence ", acronym="NMM", comments=""),
        CoursesPutRequestInner(name="New Me Strengths", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Violent offence, Sexual offence, Intimate partner violence ", acronym="NMS", comments=""),
        CoursesPutRequestInner(name="Thinking Skills Programme", description="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience="Violent offence, Intimate partner violence", acronym="TSP", comments="")

      ),
    )
  }

  private fun fromResource(resourceName: String) = this::class.java.getResource(resourceName)?.openStream() ?: throw FileNotFoundException(resourceName)
}

data class MyBean(val name: String? = null, val description: String? = null, val comment: String? = null)
