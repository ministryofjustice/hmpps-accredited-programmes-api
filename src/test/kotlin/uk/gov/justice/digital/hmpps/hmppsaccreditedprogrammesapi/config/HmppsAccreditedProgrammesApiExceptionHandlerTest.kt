package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.UUID

class HmppsAccreditedProgrammesApiExceptionHandlerTest {
  @Test
  fun `handleMethodArgumentTypeMismatchException returns ErrorResponse when a bad UUID is provided`() {
    val handler = HmppsAccreditedProgrammesApiExceptionHandler()
    val exception = MethodArgumentTypeMismatchException("bad-uuid", UUID::class.java, "uuid", mockk(), Throwable("Conversion error"))
    val response = handler.handleMethodArgumentTypeMismatchException(exception)

    assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(response.body?.userMessage).isEqualTo("Request not readable: Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Conversion error")
    assertThat(response.body?.developerMessage).isEqualTo("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Conversion error")
  }
}
