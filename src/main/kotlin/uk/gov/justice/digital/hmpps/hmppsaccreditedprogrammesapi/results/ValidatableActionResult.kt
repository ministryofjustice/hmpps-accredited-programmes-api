package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.results

import java.util.UUID

sealed interface ValidatableActionResult<EntityType> {
  fun <T> translateError(): ValidatableActionResult<T> = when (this) {
    is Success -> throw RuntimeException("Cannot translate Success")
    is FieldValidationError -> FieldValidationError(this.validationMessages)
    is GeneralValidationError -> GeneralValidationError(this.message)
    is ConflictError -> ConflictError(this.conflictingEntityId, this.message)
  }

  data class Success<EntityType>(val entity: EntityType) : ValidatableActionResult<EntityType>
  data class FieldValidationError<EntityType>(val validationMessages: ValidationErrors) :
    ValidatableActionResult<EntityType>
  data class GeneralValidationError<EntityType>(val message: String) : ValidatableActionResult<EntityType>
  data class ConflictError<EntityType>(val conflictingEntityId: UUID, val message: String) : ValidatableActionResult<EntityType>
}
