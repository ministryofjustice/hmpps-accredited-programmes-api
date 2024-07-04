package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AudienceRepository

@Service
@Transactional
class AudienceService
@Autowired
constructor(
  private val audienceRepository: AudienceRepository,
) {
  fun getAllAudiences(): List<AudienceEntity> = audienceRepository.findAll()
}
