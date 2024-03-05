package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.EnabledOrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory

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

  @MockkBean
  private lateinit var enabledOrganisationService: EnabledOrganisationService

  @Nested
  inner class GetCoursesByOrganisationIdTests {
    @Test
    fun `getAllCoursesByOrganisationId with JWT returns 200 with correct body`() {
      val offeringEntity1 =
        OfferingEntityFactory().withOrganisationId(ORGANISATION_ID_MDI).withContactEmail("of1@digital.justice.gov.uk")
          .produce()
      offeringEntity1.course = CourseEntityFactory().produce()

      val offeringEntity2 =
        OfferingEntityFactory().withOrganisationId(ORGANISATION_ID_MDI).withContactEmail("of2@digital.justice.gov.uk")
          .produce()
      offeringEntity2.course = CourseEntityFactory().produce()

      val offerings = listOf(offeringEntity1, offeringEntity2)

      every { courseService.getAllOfferingsByOrganisationId(ORGANISATION_ID_MDI) } returns offerings

      mockMvc.get("/organisations/$ORGANISATION_ID_MDI/courses") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          assertThat(offerings.size).isEqualTo(2)

          assertThat(offerings[0].organisationId).isEqualTo(ORGANISATION_ID_MDI)
          assertThat(offerings[1].organisationId).isEqualTo(ORGANISATION_ID_MDI)
          assertThat(offerings[0].contactEmail).isEqualTo("of1@digital.justice.gov.uk")
          assertThat(offerings[1].contactEmail).isEqualTo("of2@digital.justice.gov.uk")
        }
      }
    }

    @Test
    fun `getAllCoursesByOrganisationId without JWT returns 401`() {
      mockMvc.get("/organisations/$ORGANISATION_ID_MDI/courses") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }

  @Nested
  inner class GetEnabledOrganisations {

    @Test
    fun `Should return list of enabled organisations`() {
      val description = "Whatton"
      val code = "WTI"

      val enabledOrganisations = listOf(
        EnabledOrganisationEntityFactory().description(description).code(code)
          .produce(),
      )

      every { enabledOrganisationService.getEnabledOrganisations() } returns enabledOrganisations
      mockMvc.get("/organisations/enabled") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          assertThat(enabledOrganisations.size).isEqualTo(1)
          assertThat(enabledOrganisations[0].code).isEqualTo(code)
          assertThat(enabledOrganisations[0].description).isEqualTo(description)
        }
      }
    }

    @Test
    fun `getAllEnabledOrganisations without JWT returns 401`() {
      mockMvc.get("/organisations/enabled") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }
}
