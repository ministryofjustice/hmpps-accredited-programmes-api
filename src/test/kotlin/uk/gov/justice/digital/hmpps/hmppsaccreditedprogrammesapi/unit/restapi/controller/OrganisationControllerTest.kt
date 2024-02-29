package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsauditsdk.AuditService

private const val ORG_ID = "OF1"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class OrganisationControllerTest
@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var courseService: CourseService

  @MockBean
  private lateinit var auditService: AuditService

  @Nested
  inner class GetCoursesByOrganisationIdTests {
    @Test
    fun `getAllCoursesByOrganisationId with JWT returns 200 with correct body`() {
      val offeringEntity1 = OfferingEntityFactory().withOrganisationId(ORG_ID).withContactEmail("of1@digital.justice.gov.uk")
        .produce()
      offeringEntity1.course = CourseEntityFactory().produce()

      val offeringEntity2 = OfferingEntityFactory().withOrganisationId(ORG_ID).withContactEmail("of2@digital.justice.gov.uk")
        .produce()
      offeringEntity2.course = CourseEntityFactory().produce()

      val offerings = listOf(offeringEntity1, offeringEntity2)

      every { courseService.getAllOfferingsByOrganisationId(ORG_ID) } returns offerings

      mockMvc.get("/organisations/$ORG_ID/courses") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          assertThat(offerings.size).isEqualTo(2)

          assertThat(offerings[0].organisationId).isEqualTo(ORG_ID)
          assertThat(offerings[1].organisationId).isEqualTo(ORG_ID)
          assertThat(offerings[0].contactEmail).isEqualTo("of1@digital.justice.gov.uk")
          assertThat(offerings[1].contactEmail).isEqualTo("of2@digital.justice.gov.uk")
        }
      }
    }

    @Test
    fun `getAllCoursesByOrganisationId without JWT returns 401`() {
      mockMvc.get("/organisations/$ORG_ID/courses") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }
}
