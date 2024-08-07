package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SentenceDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ManageOffencesService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PeopleSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService

@RestController
@Tag(
  name = "People",
  description = """
    A series of endpoints for returning personal data.
  """,
)
class PeopleController(
  private val courseParticipationService: CourseParticipationService,
  private val personService: PersonService,
  private val manageOffencesService: ManageOffencesService,
  private val peopleSearchApiService: PeopleSearchApiService,
  private val auditService: AuditService,
) {

  @Operation(
    tags = ["Course Participations"],
    summary = "Retrieve course participation information for a person identified by their prison number.",
    operationId = "getCourseParticipationsByPrisonNumber",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "All historic course participation information for the person.  Empty if none found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = CourseParticipation::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client is not authorised to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{prisonNumber}/course-participations"],
    produces = ["application/json"],
  )
  fun getCourseParticipationsByPrisonNumber(
    @Parameter(
      description = "The prison number of the person for whom the information should be retrieved.",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<List<CourseParticipation>> {
    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.COURSE_PARTICIPATION.name,
    )
    return ResponseEntity.ok(
      courseParticipationService
        .getCourseParticipationsByPrisonNumber(prisonNumber)
        .map(CourseParticipationEntity::toApi),
    )
  }

  @Operation(
    tags = ["Person"],
    summary = "Get details of an offence by prison number",
    operationId = "getOffences",
    description = """Retrieves details of an offence by its unique code.""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Successful operation",
        content = [Content(array = ArraySchema(schema = Schema(implementation = Offence::class)))],
      ),
      ApiResponse(responseCode = "401", description = "Unauthorised. The request was unauthorised."),
      ApiResponse(responseCode = "403", description = "Forbidden.  The client is not authorised to access person."),
      ApiResponse(
        responseCode = "404",
        description = "Invalid prison number",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/offences/{prisonNumber}"],
    produces = ["application/json"],
  )
  fun getOffences(
    @Parameter(
      description = "The prison number of the person for whom the information should be retrieved.",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<List<Offence>> {
    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.OFFENCE_DETAILS.name,
    )
    val offenceMap = personService.getOffenceDetails(prisonNumber).associateBy({ it.first }, { it.second })
    val offences = manageOffencesService.getOffences(offenceMap.keys.toList())

    return ResponseEntity.ok(
      offences.map { offence ->
        Offence(
          offence = "${offence.description} - ${offence.code}",
          category = offence.legislation,
          offenceDate = offenceMap[offence.code],
        )
      },
    )
  }

  @Operation(
    tags = ["Person"],
    summary = "Sentence details",
    operationId = "getSentenceDetails",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = SentenceDetails::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person not found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{prisonNumber}/sentences"],
    produces = ["application/json"],
  )
  fun getSentenceDetails(
    @Parameter(
      description = "prison number",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<SentenceDetails> {
    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.SENTENCE_DETAILS.name,
    )
    return ResponseEntity.ok(
      personService
        .getSentenceDetails(prisonNumber),
    )
  }

  @Operation(
    tags = ["Person"],
    summary = "Search for a prisoner via prison search api by prisoner id and caseload.",
    operationId = "searchPeople",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "The prisoner search results.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = PeopleSearchResponse::class)))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad input",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client is not authorized to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/people/search", "prisoner-search"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun searchPeople(
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody peopleSearchRequest: PeopleSearchRequest,
  ): ResponseEntity<List<PeopleSearchResponse>> {
    auditService.audit(
      prisonNumber = peopleSearchRequest.prisonerIdentifier,
      auditAction = AuditAction.NOMIS_SEARCH_FOR_PERSON.name,
    )

    return ResponseEntity.ok(
      peopleSearchApiService.searchPeople(peopleSearchRequest)
        .map {
          PeopleSearchResponse(
            bookingId = it.bookingId,
            conditionalReleaseDate = it.conditionalReleaseDate,
            prisonName = it.prisonName,
            dateOfBirth = it.dateOfBirth,
            ethnicity = it.ethnicity,
            gender = it.gender,
            homeDetentionCurfewEligibilityDate = it.homeDetentionCurfewEligibilityDate,
            indeterminateSentence = it.indeterminateSentence,
            firstName = it.firstName,
            lastName = it.lastName,
            paroleEligibilityDate = it.paroleEligibilityDate,
            prisonerNumber = it.prisonerNumber,
            religion = it.religion,
            sentenceExpiryDate = it.sentenceExpiryDate,
            sentenceStartDate = it.sentenceStartDate,
            tariffDate = it.tariffDate,
          )
        },
    )
  }
}
