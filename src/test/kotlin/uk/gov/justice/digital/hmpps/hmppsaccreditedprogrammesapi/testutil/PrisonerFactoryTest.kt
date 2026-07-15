package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PrisonerFactoryTest {

  @Test
  fun `should create a prisoner with default values`() {
    val prisoner = PrisonerFactory().produce()

    assertThat(prisoner.prisonerNumber).isEqualTo("C6666CC")
    assertThat(prisoner.bookingId).isEqualTo("1201102")
    assertThat(prisoner.firstName).isEqualTo("JOHN")
    assertThat(prisoner.lastName).isEqualTo("SMITH")
    assertThat(prisoner.prisonName).isEqualTo("Transfer")
    assertThat(prisoner.gender).isEqualTo("Male")
    assertThat(prisoner.indeterminateSentence).isFalse()
  }

  @Test
  fun `should create a prisoner with custom values`() {
    val conditionalReleaseDate = LocalDate.now()
    val prisoner = PrisonerFactory()
      .withPrisonerNumber("A1234BC")
      .withFirstName("Jane")
      .withLastName("Doe")
      .withConditionalReleaseDate(conditionalReleaseDate)
      .produce()

    assertThat(prisoner.prisonerNumber).isEqualTo("A1234BC")
    assertThat(prisoner.firstName).isEqualTo("Jane")
    assertThat(prisoner.lastName).isEqualTo("Doe")
    assertThat(prisoner.conditionalReleaseDate).isEqualTo(conditionalReleaseDate)

    // Check that other defaults still apply
    assertThat(prisoner.bookingId).isEqualTo("1201102")
  }
}
