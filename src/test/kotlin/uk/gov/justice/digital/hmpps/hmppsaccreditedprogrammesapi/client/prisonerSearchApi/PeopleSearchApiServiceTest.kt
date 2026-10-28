package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PeopleSearchApiService

class PeopleSearchApiServiceTest {

  private val prisonerSearchApiClient = mockk<PrisonerSearchApiClient>()
  val service = PeopleSearchApiService(prisonerSearchApiClient)

  @Test
  fun `should return list of prisoners`() {
    val prisonerDetails = listOf(PRISONER_1)

    every { prisonerSearchApiClient.getPrisonersByPrisonNumbers(any()) } returns ClientResult.Success(HttpStatus.OK, prisonerDetails)

    val result = service.getPrisoners(listOf(PRISON_NUMBER_1))
    assertEquals(listOf(PRISONER_1), result)
  }

  @Test
  fun `get prisoners call returns empty list when there is a failure fetching prisoners details`() {
    every { prisonerSearchApiClient.getPrisonersByPrisonNumbers(any()) } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/prisoner-search/prisoner-numbers",
      HttpStatusCode.valueOf(400),
      "",
    )

    val result = service.getPrisoners(listOf(PRISON_NUMBER_1))

    assertTrue(result.isEmpty())
  }

  @Test
  fun `searchPeople returns matches when prisoner-search finds the person`() {
    val request = PeopleSearchRequest(prisonerIdentifier = PRISON_NUMBER_1)
    val match = peopleSearchResponse(PRISON_NUMBER_1)

    every { prisonerSearchApiClient.peopleSearch(request) } returns ClientResult.Success(HttpStatus.OK, listOf(match))

    val result = service.searchPeople(request)

    assertEquals(listOf(match), result)
  }

  @Test
  fun `searchPeople returns empty list when prisoner-search returns a successful but empty match (person not found)`() {
    val request = PeopleSearchRequest(prisonerIdentifier = PRISON_NUMBER_1)

    every { prisonerSearchApiClient.peopleSearch(request) } returns ClientResult.Success(HttpStatus.OK, emptyList())

    val result = service.searchPeople(request)

    assertTrue(result.isEmpty())
  }

  private fun peopleSearchResponse(prisonerNumber: String) = PeopleSearchResponse(
    bookingId = null,
    conditionalReleaseDate = null,
    prisonId = null,
    prisonName = null,
    dateOfBirth = null,
    ethnicity = null,
    gender = null,
    homeDetentionCurfewEligibilityDate = null,
    indeterminateSentence = null,
    firstName = null,
    lastName = null,
    paroleEligibilityDate = null,
    prisonerNumber = prisonerNumber,
    religion = null,
    sentenceExpiryDate = null,
    sentenceStartDate = null,
    tariffDate = null,
  )
}
