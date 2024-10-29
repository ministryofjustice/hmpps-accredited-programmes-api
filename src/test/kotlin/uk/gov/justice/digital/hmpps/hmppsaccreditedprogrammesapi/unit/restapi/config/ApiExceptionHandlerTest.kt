package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.config

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config.ApiExceptionHandler
import java.util.UUID

class ApiExceptionHandlerTest {

  val handler = ApiExceptionHandler()

  @Test
  fun `handleMethodArgumentTypeMismatchException with invalid UUID returns 400 with error response`() {
    val exception = MethodArgumentTypeMismatchException("bad-uuid", UUID::class.java, "uuid", mockk(), Throwable("Conversion error"))
    val response = handler.handleMethodArgumentTypeMismatchException(exception)

    assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(response.body?.userMessage).isEqualTo("Request not readable: Method parameter 'uuid': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Conversion error")
    assertThat(response.body?.developerMessage).isEqualTo("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Conversion error")
  }

  @Test
  fun `prisonApiUnavailableHandledAsExpected 503 error response with appropriate message`() {
    val exception = ServiceUnavailableException("Prison API is unavailable", Throwable("Service is down"))
    val response = handler.handleServiceUnavailableException(exception)

    assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    assertThat(response.body?.userMessage).isEqualTo("Service unavailable: Prison API is unavailable")
    assertThat(response.body?.developerMessage).isEqualTo("Prison API is unavailable")
  }
}
