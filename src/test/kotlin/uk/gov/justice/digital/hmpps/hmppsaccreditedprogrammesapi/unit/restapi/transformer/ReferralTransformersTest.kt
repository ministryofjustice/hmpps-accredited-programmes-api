package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.transformer

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate as DomainReferralUpdate

class ReferralTransformersTest {

  @Test
  fun `Transforming a ReferralUpdate with all fields should convert to its Domain equivalent`() {
    val apiModel = ApiReferralUpdate(
      additionalInformation = "Additional Info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    with(apiModel.toDomain()) {
      additionalInformation shouldBe apiModel.additionalInformation
      oasysConfirmed shouldBe apiModel.oasysConfirmed
      hasReviewedProgrammeHistory shouldBe apiModel.hasReviewedProgrammeHistory
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all nullable fields should tolerantly convert to Domain`() {
    val apiModel = ApiReferralUpdate(
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
    )

    with(apiModel.toDomain()) {
      additionalInformation shouldBe null
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all fields should convert to its API equivalent`() {
    val domainModel = DomainReferralUpdate(
      additionalInformation = "Additional Info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    with(domainModel.toApi()) {
      additionalInformation shouldBe domainModel.additionalInformation
      oasysConfirmed shouldBe domainModel.oasysConfirmed
      hasReviewedProgrammeHistory shouldBe domainModel.hasReviewedProgrammeHistory
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all nullable fields should tolerantly convert to API`() {
    val domainModel = DomainReferralUpdate(
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
    )

    with(domainModel.toApi()) {
      additionalInformation shouldBe null
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }
}
