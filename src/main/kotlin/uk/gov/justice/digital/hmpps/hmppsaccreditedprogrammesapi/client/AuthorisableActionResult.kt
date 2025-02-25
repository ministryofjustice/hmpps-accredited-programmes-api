package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client

sealed interface AuthorisableActionResult<EntityType> {
  data class Success<EntityType>(val entity: EntityType) : AuthorisableActionResult<EntityType>
  class Unauthorised<EntityType> : AuthorisableActionResult<EntityType>
  class NotFound<EntityType>(val entityType: String? = null, val id: String? = null) : AuthorisableActionResult<EntityType>
}
