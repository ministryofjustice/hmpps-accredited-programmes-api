package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import java.math.BigInteger

/**
 * Resolves staff identifiers (usernames or numeric staff IDs) to the staff member's
 * surname, for use in the custody Subject Access Request (SAR) report.
 *
 * Returns `null` when no matching staff row exists; the SAR mustache template renders
 * `No Data Held` (via the `optionalValue` helper) for null values.
 *
 * Kept as a service (rather than calling the repository directly) so that future
 * cross-cutting concerns – caching, batching, or a change of fallback rule – have a
 * single home.
 */
@Service
@Transactional(readOnly = true)
class StaffLookupService(
  private val staffRepository: StaffRepository,
) {
  fun findLastNameByUsername(username: String?): String? = username?.takeIf { it.isNotBlank() }?.let { staffRepository.findLastNameByUsername(it).firstOrNull() }

  fun findLastNameByStaffId(staffId: BigInteger?): String? = staffId?.let { staffRepository.findLastNameByStaffId(it).firstOrNull() }
}
