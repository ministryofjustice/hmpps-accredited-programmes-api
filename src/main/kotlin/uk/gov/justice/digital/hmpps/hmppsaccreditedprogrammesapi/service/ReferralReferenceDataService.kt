package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusCategory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.getByCode

@Service
class ReferralReferenceDataService(
  private val referralStatusRepository: ReferralStatusRepository,
  private val referralStatusCategoryRepository: ReferralStatusCategoryRepository,
  private val referralStatusReasonRepository: ReferralStatusReasonRepository,
) {
  fun getReferralStatuses() =
    referralStatusRepository.findAllByActiveIsTrue()
      .map {
        ReferralStatusRefData(
          code = it.code,
          description = it.description,
          colour = it.colour,
          closed = it.closed,
          draft = it.draft,
        )
      }

  fun getReferralStatus(code: String) =
    referralStatusRepository.getByCode(code).let {
      ReferralStatusRefData(it.code, it.description, it.colour, it.closed, it.draft)
    }

  fun getReferralStatusCategories(statusCode: String) =
    referralStatusCategoryRepository.getAllByReferralStatusCodeAndActiveIsTrue(statusCode).map {
      ReferralStatusCategory(
        code = it.code,
        description = it.description,
        referralStatusCode = it.referralStatusCode,
      )
    }

  fun getReferralStatusCategory(code: String) =
    referralStatusCategoryRepository.getByCode(code).let {
      ReferralStatusCategory(
        code = it.code,
        description = it.description,
        referralStatusCode = it.referralStatusCode,
      )
    }

  fun getReferralStatusReasons(referralStatusCode: String, referralCategoryCode: String) =
    referralStatusReasonRepository.getAllByReferralStatusCategoryCodeAndActiveIsTrue(referralCategoryCode).map {
      ReferralStatusReason(
        code = it.code,
        description = it.description,
        referralCategoryCode = it.referralStatusCategoryCode,
      )
    }

  fun getReferralStatusReason(code: String) =
    referralStatusReasonRepository.getByCode(code).let {
      ReferralStatusReason(
        code = it.code,
        description = it.description,
        referralCategoryCode = it.referralStatusCategoryCode,
      )
    }
}
