package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.EnabledOrganisation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.EnabledOrganisationRepository

@Service
class EnabledOrganisationService(val enabledOrganisationRepository: EnabledOrganisationRepository) {
  fun getEnabledOrganisations(): List<EnabledOrganisation> = enabledOrganisationRepository.findAll()
  fun getEnabledOrganisation(organisationId: String) = enabledOrganisationRepository.findEnabledOrganisationByCode(organisationId)
}
