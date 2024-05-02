package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.PrisonSearchApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Address
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Category
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonOperator
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService

@Service
class PrisonSearchController
@Autowired
constructor(
  private val prisonRegisterApiService: PrisonRegisterApiService,
) : PrisonSearchApiDelegate {

  override fun getPrisons(prisonSearchRequest: PrisonSearchRequest): ResponseEntity<List<PrisonSearchResponse>> {
    return ResponseEntity.ok(
      prisonRegisterApiService.getPrisons(prisonSearchRequest.prisonIds)
        .map { prison ->
          PrisonSearchResponse(
            prisonId = prison.prisonId,
            prisonName = prison.prisonName,
            active = prison.active,
            male = prison.male,
            female = prison.female,
            contracted = prison.contracted,
            types = prison.types.map { PrisonType(it.code, it.description) },
            categories = prison.categories.map { Category(it) },
            addresses = prison.addresses.map {
              Address(
                id = it.id,
                addressLine1 = it.addressLine1,
                addressLine2 = it.addressLine2,
                county = it.county,
                town = it.town,
                postcode = it.postcode,
                country = it.country,
              )
            },
            operators = prison.operators.map { PrisonOperator(name = it.name) },
          )
        },
    )
  }
}
