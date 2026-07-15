package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener

enum class HmppsDomainEventTypes(val value: String) {
  PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED("prisoner-offender-search.prisoner.updated"),
  OFFENDER_MANAGEMENT_ALLOCATION_CHANGED("offender-management.allocation.changed"),
  PROBATION_CASE_REQUIREMENT_CREATED("probation-case.requirement.created"),
  PRISON_OFFENDER_EVENTS_PRISONER_MERGED("prison-offender-events.prisoner.merged"),
}
