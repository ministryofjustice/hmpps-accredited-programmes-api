package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Entity
@Table(name = "referral_status_transitions")
@Immutable
class ReferralStatusTransitionEntity(
  @Id
  @Column(name = "referral_status_transition_id")
  val id: UUID,
  val ptUser: Boolean,
  val pomUser: Boolean,
  @ManyToOne
  @JoinColumn(name = "transitionFromStatus")
  val fromStatus: ReferralStatusEntity,
  @ManyToOne
  @JoinColumn(name = "transitionToStatus")
  val toStatus: ReferralStatusEntity,
)

@Repository
interface ReferralStatusTransitionRepository : JpaRepository<ReferralStatusTransitionEntity, UUID> {

  @EntityGraph(attributePaths = ["fromStatus", "toStatus"])
  @Query(
    """
      select st from ReferralStatusTransitionEntity st
      where st.fromStatus.code = :fromStatus
      and st.ptUser = true
    """,
  )
  fun getNextPTTransitions(fromStatus: String): List<ReferralStatusTransitionEntity>

  @EntityGraph(attributePaths = ["fromStatus", "toStatus"])
  @Query(
    """
      select st from ReferralStatusTransitionEntity st
      where st.fromStatus.code = :fromStatus
      and st.pomUser = true
    """,
  )
  fun getNextPOMTransitions(fromStatus: String): List<ReferralStatusTransitionEntity>
}
