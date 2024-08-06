package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DrugAlcoholDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.LearningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OasysAssessmentDateInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RoshAnalysis
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.OasysService

@RestController
@Tag(
  name = "Oasys",
  description = """
    A series of endpoints for returning Oasys data.
  """,
)
@Transactional
class OasysController(val oasysService: OasysService) {

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Attitude details as held by Oasys",
    operationId = "getAttitude",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Attitude::class))],
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
    value = ["/oasys/{prisonNumber}/attitude"],
    produces = ["application/json"],
  )
  fun getAttitude(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<Attitude> =
    ResponseEntity
      .ok(
        oasysService
          .getAttitude(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Behaviour details as held by Oasys",
    operationId = "getBehaviour",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Behaviour::class))],
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
    value = ["/oasys/{prisonNumber}/behaviour"],
    produces = ["application/json"],
  )
  fun getBehaviour(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<Behaviour> =
    ResponseEntity
      .ok(
        oasysService
          .getBehaviour(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Get drug and alcohol assessment details",
    operationId = "getDrugAndAlcoholDetails",
    description = """Retrieves the drug and alcohol assessment details for a given prison number""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = DrugAlcoholDetail::class))],
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
    value = ["/oasys/{prisonNumber}/drug-and-alcohol-details"],
    produces = ["application/json"],
  )
  fun getDrugAndAlcoholDetails(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<DrugAlcoholDetail> =
    ResponseEntity
      .ok(
        oasysService
          .getDrugAndAlcoholDetail(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Health details as held by Oasys",
    operationId = "getHealth",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Health::class))],
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
    value = ["/oasys/{prisonNumber}/health"],
    produces = ["application/json"],
  )
  fun getHealth(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<Health> =
    ResponseEntity
      .ok(
        oasysService
          .getHealth(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Get latest layer 3 completed assessment date",
    operationId = "getLatestCompletedAssessmentDate",
    description = """Get latest layer 3 completed assessment date and if there are any open assessments""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = OasysAssessmentDateInfo::class))],
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
    value = ["/oasys/{prisonNumber}/assessment_date"],
    produces = ["application/json"],
  )
  fun getLatestCompletedAssessmentDate(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<OasysAssessmentDateInfo> =
    ResponseEntity
      .ok(
        oasysService
          .getAssessmentDateInfo(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Learning needs details as held by Oasys",
    operationId = "getLearningNeeds",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = LearningNeeds::class))],
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
    value = ["/oasys/{prisonNumber}/learning-needs"],
    produces = ["application/json"],
  )
  fun getLearningNeeds(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<LearningNeeds> =
    ResponseEntity
      .ok(
        oasysService
          .getLearningNeeds(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Lifestyle details as held by Oasys",
    operationId = "getLifestyle",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Lifestyle::class))],
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
    value = ["/oasys/{prisonNumber}/lifestyle"],
    produces = ["application/json"],
  )
  fun getLifestyle(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<Lifestyle> =
    ResponseEntity
      .ok(
        oasysService
          .getLifestyle(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Offence details as held by Oasys",
    operationId = "getOffenceDetails",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = OffenceDetail::class))],
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
    value = ["/oasys/{prisonNumber}/offence-details"],
    produces = ["application/json"],
  )
  fun getOffenceDetails(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<OffenceDetail> =
    ResponseEntity
      .ok(
        oasysService
          .getOffenceDetail(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Psychiatric details as held by Oasys",
    operationId = "getPsychiatric",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Psychiatric::class))],
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
    value = ["/oasys/{prisonNumber}/psychiatric"],
    produces = ["application/json"],
  )
  fun getPsychiatric(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<Psychiatric> =
    ResponseEntity
      .ok(
        oasysService
          .getPsychiatric(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Relationships details as held by Oasys",
    operationId = "getRelationships",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Relationships::class))],
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
    value = ["/oasys/{prisonNumber}/relationships"],
    produces = ["application/json"],
  )
  fun getRelationships(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<Relationships> =
    ResponseEntity
      .ok(
        oasysService
          .getRelationships(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Risks details as held by Oasys",
    operationId = "getRisks",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Risks::class))],
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
    value = ["/oasys/{prisonNumber}/risks-and-alerts"],
    produces = ["application/json"],
  )
  fun getRisks(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<Risks> =
    ResponseEntity
      .ok(
        oasysService
          .getRisks(prisonNumber),
      )

  @Operation(
    tags = ["Oasys Integration"],
    summary = "ROSH analysis details as held by Oasys",
    operationId = "getRoshAnalysis",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = RoshAnalysis::class))],
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
    value = ["/oasys/{prisonNumber}/rosh-analysis"],
    produces = ["application/json"],
  )
  fun getRoshAnalysis(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
  ): ResponseEntity<RoshAnalysis> =
    ResponseEntity
      .ok(
        oasysService
          .getRoshFull(prisonNumber),
      )
}
