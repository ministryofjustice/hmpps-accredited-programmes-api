package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusTransitionEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusTransitionRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.getByCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusCategory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toModel

@Service
class ReferralReferenceDataService(
  private val referralStatusRepository: ReferralStatusRepository,
  private val referralStatusCategoryRepository: ReferralStatusCategoryRepository,
  private val referralStatusReasonRepository: ReferralStatusReasonRepository,
  private val referralStatusTransitionRepository: ReferralStatusTransitionRepository,
) {
  fun getReferralStatuses() = referralStatusRepository.findAllByActiveIsTrueOrderByDefaultOrder()
    .map {
      ReferralStatusRefData(
        code = it.code,
        description = it.description,
        hintText = it.hintText,
        hasNotes = it.hasNotes,
        hasConfirmation = it.hasConfirmation,
        confirmationText = it.confirmationText,
        colour = it.colour,
        closed = it.closed,
        draft = it.draft,
        hold = it.hold,
        release = it.release,
        notesOptional = it.notesOptional,
      )
    }

  fun getReferralStatus(code: String) = referralStatusRepository.getByCode(code).let {
    ReferralStatusRefData(
      code = it.code,
      description = it.description,
      colour = it.colour,
      hintText = it.hintText,
      hasNotes = it.hasNotes,
      hasConfirmation = it.hasConfirmation,
      confirmationText = it.confirmationText,
      closed = it.closed,
      draft = it.draft,
      hold = it.hold,
      release = it.release,
      notesOptional = it.notesOptional,
    )
  }

  fun getReferralStatusCategories(statusCode: String) = referralStatusCategoryRepository.getAllByReferralStatusCodeAndActiveIsTrue(statusCode).map {
    ReferralStatusCategory(
      code = it.code,
      description = it.description,
      referralStatusCode = it.referralStatusCode,
    )
  }

  fun getReferralStatusCategory(code: String) = referralStatusCategoryRepository.getByCode(code).let {
    ReferralStatusCategory(
      code = it.code,
      description = it.description,
      referralStatusCode = it.referralStatusCode,
    )
  }

  fun getReferralStatusReasons(
    referralStatusCode: String,
    referralCategoryCode: String,
    deselectAndKeepOpen: Boolean = false,
  ) = referralStatusReasonRepository.getAllByReferralStatusCategoryCodeAndActiveIsTrue(
    referralCategoryCode,
    deselectAndKeepOpen,
  ).map {
    ReferralStatusReason(
      code = it.code,
      description = it.description,
      referralCategoryCode = it.referralStatusCategoryCode,
      categoryDescription = null,
    )
  }

  fun getReferralStatusReason(code: String) = referralStatusReasonRepository.getByCode(code).let {
    ReferralStatusReason(
      code = it.code,
      description = it.description,
      referralCategoryCode = it.referralStatusCategoryCode,
      categoryDescription = null,
    )
  }

  fun getNextStatusTransitions(currentStatus: String, ptRole: Boolean = false): List<ReferralStatusRefData> = if (ptRole) {
    referralStatusTransitionRepository.getNextPTTransitions(currentStatus)
      .map { it.toStatus.toModel(it.description, it.hintText) }
  } else {
    referralStatusTransitionRepository.getNextPOMTransitions(currentStatus)
      .map { it.toStatus.toModel(it.description, it.hintText) }
  }

  fun getStatusTransition(
    currentStatus: String,
    chosenStatus: String,
    ptRole: Boolean = false,
  ): ReferralStatusTransitionEntity? = if (ptRole) {
    referralStatusTransitionRepository.getPTTransition(currentStatus, chosenStatus)
  } else {
    referralStatusTransitionRepository.getPOMTransition(currentStatus, chosenStatus)
  }

  fun getAllReferralStatusReasonsForType(referralStatusType: ReferralStatusType, deselectAndKeepOpen: Boolean): List<ReferralStatusReason> = referralStatusReasonRepository.findReferralStatusReasonsByStatusCode(referralStatusType.name, deselectAndKeepOpen)
    .map { it.toModel() }
}
