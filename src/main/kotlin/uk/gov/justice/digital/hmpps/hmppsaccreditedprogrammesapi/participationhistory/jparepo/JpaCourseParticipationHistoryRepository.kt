package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.jparepo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import java.util.UUID

@Repository
interface JpaCourseParticipationHistoryRepository : JpaRepository<CourseParticipationHistory, UUID>
