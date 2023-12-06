package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.results

import java.util.UUID

@JvmInline
value class ValidationErrors(private val errorMap: MutableMap<String, String>) : MutableMap<String, String> by errorMap {
  constructor() : this(mutableMapOf())

  override fun toString() = errorMap.map { "${it.key}: ${it.value}" }.joinToString(",")
}

private fun singleValidationErrorOf(propertyNameToMessage: Pair<String, String>) = ValidationErrors().apply { this[propertyNameToMessage.first] = propertyNameToMessage.second }

class ValidatedScope<EntityType> {
  val validationErrors = ValidationErrors()

  val fieldValidationError: ValidatableActionResult.FieldValidationError<EntityType> = ValidatableActionResult.FieldValidationError(validationErrors)

  infix fun success(entity: EntityType) = ValidatableActionResult.Success(entity)
  infix fun generalError(message: String) = ValidatableActionResult.GeneralValidationError<EntityType>(message)
  infix fun String.hasValidationError(message: String) = validationErrors.put(this, message)
  infix fun String.hasSingleValidationError(message: String) = ValidatableActionResult.FieldValidationError<EntityType>(singleValidationErrorOf(this to message))
  infix fun UUID.hasConflictError(message: String) = ValidatableActionResult.ConflictError<EntityType>(this, message)
}

inline fun <EntityType> validated(scope: ValidatedScope<EntityType>.() -> ValidatableActionResult<EntityType>): ValidatableActionResult<EntityType> {
  return scope(ValidatedScope())
}
