package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception

open class ServiceUnavailableException(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)

class PrisonApiUnavailableException(message: String?, cause: Throwable? = null) : ServiceUnavailableException(message, cause)
