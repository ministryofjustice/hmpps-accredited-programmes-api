package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_ID_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NAME_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
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
  private lateinit var prisonRegisterApiService: PrisonRegisterApiService

  @Nested
  inner class GetCoursesByOrganisationIdTests {
    @Test
    fun `getAllCoursesByOrganisationId with JWT returns 200 with correct body`() { // TODO extend this to include withdrawn courses
      val offeringEntity1 =
        OfferingEntityFactory().withOrganisationId(ORGANISATION_ID_MDI).withContactEmail("of2@digital.justice.gov.uk")
          .produce()
      offeringEntity1.course = CourseEntityFactory().produce()

      val offeringEntity2 =
        OfferingEntityFactory().withOrganisationId(ORGANISATION_ID_MDI).withContactEmail("of2@digital.justice.gov.uk")
          .produce()
      offeringEntity2.course = CourseEntityFactory().produce()

      val offeringEntity3 =
        OfferingEntityFactory().withOrganisationId(ORGANISATION_ID_MDI).withContactEmail("of2@digital.justice.gov.uk")
          .produce()
      offeringEntity3.course = CourseEntityFactory().withWithdrawn(true).produce()

      val offerings = listOf(offeringEntity1, offeringEntity2, offeringEntity3)

      every { courseService.getAllOfferingsByOrganisationId(ORGANISATION_ID_MDI) } returns offerings

      mockMvc.get("/organisations/$ORGANISATION_ID_MDI/courses") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          assertThat(offerings.size).isEqualTo(3)
          assertThat(offerings)
            .extracting(OfferingEntity::organisationId, OfferingEntity::contactEmail)
            .containsOnly(tuple(ORGANISATION_ID_MDI, "of2@digital.justice.gov.uk"))
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
  inner class GetAllOrganisations {
    @Test
    fun `Should return list of all organisations`() {
      val prison = Prison(
        prisonId = PRISON_ID_1,
        prisonName = PRISON_NAME_1,
      )

      every { prisonRegisterApiService.getPrisons() } returns listOf(prison)
      mockMvc.get("/organisations") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          assertThat(prison.prisonId).isEqualTo(PRISON_ID_1)
          assertThat(prison.prisonName).isEqualTo(PRISON_NAME_1)
        }
      }
    }
  }
}
