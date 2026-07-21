package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
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
 *
 * The batch variants [resolveSurnamesByUsername] / [resolveSurnamesByStaffId] should
 * be preferred when resolving many identifiers at once (e.g. building a SAR content
 * payload) as they collapse N lookups into a single query per identifier type.
 */
@Service
@Transactional(readOnly = true)
class StaffLookupService(
  private val staffRepository: StaffRepository,
) {
  private val log = LoggerFactory.getLogger(StaffLookupService::class.java)

  fun findLastNameByUsername(username: String?): String? = username?.takeIf { it.isNotBlank() }?.let { staffRepository.findLastNameByUsername(it).firstOrNull() }

  fun findLastNameByStaffId(staffId: BigInteger?): String? = staffId?.let { staffRepository.findLastNameByStaffId(it).firstOrNull() }

  /**
   * Batch-resolve a set of usernames to surnames in a single query.
   *
   * Nulls and blank strings are filtered out before hitting the database; the
   * returned map only contains entries for usernames with at least one matching
   * staff row. When a username maps to multiple staff rows (which the schema
   * permits – see V144 migration notes) the first row is retained (ordered by
   * `staff.id` in JPQL for stability across repeated calls) and a WARN is
   * logged so duplicates remain visible in production telemetry.
   */
  fun resolveSurnamesByUsername(usernames: Collection<String?>): Map<String, String> {
    val cleaned = usernames.filterNotNull().filter { it.isNotBlank() }.toSet()
    if (cleaned.isEmpty()) return emptyMap()
    val rows = staffRepository.findSurnamesByUsernames(cleaned)
    return rows.groupBy { it.username }.mapValues { (username, matches) ->
      if (matches.size > 1) {
        log.warn(
          "Multiple staff rows found for username='{}' ({} rows); using the first surname.",
          username,
          matches.size,
        )
      }
      matches.first().lastName
    }
  }

  /**
   * Batch-resolve a set of numeric staff IDs to surnames in a single query.
   *
   * Nulls are filtered out before hitting the database; the returned map only
   * contains entries for staff IDs with at least one matching staff row. When a
   * staff ID maps to multiple rows the first row is retained (ordered by
   * `staff.id` in JPQL for stability across repeated calls) and a WARN is
   * logged so duplicates remain visible in production telemetry.
   */
  fun resolveSurnamesByStaffId(staffIds: Collection<BigInteger?>): Map<BigInteger, String> {
    val cleaned = staffIds.filterNotNull().toSet()
    if (cleaned.isEmpty()) return emptyMap()
    val rows = staffRepository.findSurnamesByStaffIds(cleaned)
    return rows.groupBy { it.staffId }.mapValues { (staffId, matches) ->
      if (matches.size > 1) {
        log.warn(
          "Multiple staff rows found for staffId={} ({} rows); using the first surname.",
          staffId,
          matches.size,
        )
      }
      matches.first().lastName
    }
  }
}
