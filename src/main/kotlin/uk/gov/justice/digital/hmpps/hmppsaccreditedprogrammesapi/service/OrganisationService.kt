package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.Gender
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import java.util.UUID

@Service
@Transactional
class OrganisationService(
  private val organisationRepository: OrganisationRepository,
  private val prisonRegisterApiService: PrisonRegisterApiService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun createOrganisationIfNotPresent(code: String, prison: Prison? = null): OrganisationEntity {
    val organisation = organisationRepository.findOrganisationEntityByCode(code)

    if (organisation == null) {
      val prisonById = prison ?: prisonRegisterApiService.getPrisonById(code)!!

      prisonById.let {
        try {
          val gender = when {
            it.male -> "MALE"
            it.female -> "FEMALE"
            else -> {
              log.warn("Prison gender could not be determined for $it")
              throw BusinessException("$prisonById does not have gender information")
            }
          }

          return organisationRepository.save(
            OrganisationEntity(
              id = UUID.randomUUID(),
              code = it.prisonId,
              name = it.prisonName,
              gender = Gender.valueOf(gender),
            ),
          )
        } catch (e: Exception) {
          log.warn("Failed to save organisation details for prison $code", e)
          throw BusinessException("Failed to save organisation details for prison $code", e)
        }
      }
    }
    return organisation
  }

  fun findOrganisationEntityByName(personLocation: String) = organisationRepository.findOrganisationEntityByName(personLocation)
  fun findOrganisationEntityByCode(code: String) = organisationRepository.findOrganisationEntityByCode(code)
}
